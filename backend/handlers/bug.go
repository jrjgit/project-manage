package handlers

import (
	"errors"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"management/config"
	"management/db"
	"management/dto"
	"management/models"
	"management/services"
)

// BugHandler Bug Handler
type BugHandler struct {
	notifier *services.NotificationService
}

// NewBugHandler 创建BugHandler
func NewBugHandler(cfg *config.Config) *BugHandler {
	return &BugHandler{
		notifier: services.NewNotificationService(cfg),
	}
}

// ========== 状态机校验 ==========

func isValidBugTransition(role, from, to string) bool {
	validTransitions := map[string][]string{
		models.BugNew:           {models.BugAssigned},
		models.BugAssigned:      {models.BugFixing},
		models.BugFixing:        {models.BugFixed},
		models.BugFixed:         {models.BugPendingVerify},
		models.BugPendingVerify: {models.BugClosed, models.BugReopened},
		models.BugReopened:      {models.BugAssigned},
	}

	allowed, ok := validTransitions[from]
	if !ok {
		return false
	}

	for _, s := range allowed {
		if s == to {
			if from == models.BugAssigned && to == models.BugFixing && role != models.RoleDev {
				return false
			}
			if from == models.BugFixing && to == models.BugFixed && role != models.RoleDev {
				return false
			}
			if from == models.BugPendingVerify && (to == models.BugClosed || to == models.BugReopened) && role != models.RoleTester {
				return false
			}
			if from == models.BugReopened && to == models.BugAssigned && (role != models.RoleTester && role != models.RolePM) {
				return false
			}
			return true
		}
	}
	return false
}

// ========== 统一状态变更入口 ==========

// ChangeBugStatus 统一Bug状态变更入口
func (h *BugHandler) ChangeBugStatus(bugID uint, newStatus string, operatorID uint, comment string) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		var bug models.Bug
		if err := tx.First(&bug, bugID).Error; err != nil {
			return err
		}

		var operator models.User
		if err := tx.First(&operator, operatorID).Error; err != nil {
			return err
		}

		if !isValidBugTransition(operator.Role, bug.Status, newStatus) {
			return errors.New("invalid status transition")
		}

		oldStatus := bug.Status
		bug.Status = newStatus
		bug.UpdatedAt = time.Now()

		if err := tx.Save(&bug).Error; err != nil {
			return err
		}

		history := models.BugStatusHistory{
			BugID:      bugID,
			FromStatus: oldStatus,
			ToStatus:   newStatus,
			ChangedBy:  operatorID,
			Comment:    comment,
		}
		if err := tx.Create(&history).Error; err != nil {
			return err
		}

		targets := h.resolveBugNotifyTargets(tx, &bug, oldStatus, newStatus)
		h.notifier.EmitBugEvent(&bug, oldStatus, newStatus, &operator, targets, comment)

		if newStatus == models.BugFixed {
			bug.Status = models.BugPendingVerify
			bug.UpdatedAt = time.Now()
			if err := tx.Save(&bug).Error; err != nil {
				return err
			}

			autoHistory := models.BugStatusHistory{
				BugID:      bugID,
				FromStatus: models.BugFixed,
				ToStatus:   models.BugPendingVerify,
				ChangedBy:  0,
				Comment:    "系统自动将 Bug 置为待验证",
			}
			if err := tx.Create(&autoHistory).Error; err != nil {
				return err
			}

			verifyTargets := h.resolveBugNotifyTargets(tx, &bug, models.BugFixed, models.BugPendingVerify)
			h.notifier.EmitBugEvent(&bug, models.BugFixed, models.BugPendingVerify, &operator, verifyTargets, "Bug 已进入待验证")
		}

		return nil
	})
}

func (h *BugHandler) resolveBugNotifyTargets(tx *gorm.DB, bug *models.Bug, oldStatus, newStatus string) []models.User {
	var targets []models.User

	switch oldStatus + "->" + newStatus {
	case models.BugNew + "->" + models.BugAssigned,
		models.BugReopened + "->" + models.BugAssigned:
		// 通知被指派开发
		if bug.AssigneeID != nil {
			var u models.User
			if err := tx.First(&u, *bug.AssigneeID).Error; err == nil {
				targets = append(targets, u)
			}
		}

	case models.BugAssigned + "->" + models.BugFixing:
		// 通知创建者（测试）
		var u models.User
		if err := tx.First(&u, bug.CreatorID).Error; err == nil {
			targets = append(targets, u)
		}

	case models.BugFixing + "->" + models.BugFixed,
		models.BugFixed + "->" + models.BugPendingVerify:
		// 通知创建者（测试）
		var u models.User
		if err := tx.First(&u, bug.CreatorID).Error; err == nil {
			targets = append(targets, u)
		}

	case models.BugPendingVerify + "->" + models.BugClosed,
		models.BugPendingVerify + "->" + models.BugReopened:
		// 通知开发
		if bug.AssigneeID != nil {
			var u models.User
			if err := tx.First(&u, *bug.AssigneeID).Error; err == nil {
				targets = append(targets, u)
			}
		}
	}

	return targets
}

// ========== HTTP Handlers ==========

// ListBugs 获取Bug列表（按角色过滤）
func (h *BugHandler) ListBugs(c *gin.Context) {
	userID := c.GetUint("userID")
	userRole := c.GetString("userRole")
	groupIDVal, _ := c.Get("groupID")
	var groupID *uint
	if gid, ok := groupIDVal.(*uint); ok && gid != nil {
		groupID = gid
	}

	var bugs []models.Bug
	query := db.DB.Model(&models.Bug{}).Preload("Task").Preload("Creator").Preload("Assignee")

	switch userRole {
	case models.RolePM, models.RoleTesterLead:
		// PM 和测试组长查看所有
	case models.RoleDevLead:
		// 开发组长看本组 Bug
		if groupID != nil {
			query = query.Where("assignee_id IN (?)",
				db.DB.Model(&models.User{}).Select("id").Where("group_id = ?", *groupID))
		} else {
			query = query.Where("assignee_id = ?", userID)
		}
	case models.RoleDev:
		query = query.Where("assignee_id = ?", userID)
	case models.RoleTester:
		query = query.Where("creator_id = ?", userID)
	default:
		query = query.Where("1=0")
	}

	if taskID := c.Query("task_id"); taskID != "" {
		query = query.Where("task_id = ?", taskID)
	}
	if status := c.Query("status"); status != "" {
		query = query.Where("status = ?", status)
	}
	if severity := c.Query("severity"); severity != "" {
		query = query.Where("severity = ?", severity)
	}

	if err := query.Order("updated_at DESC").Find(&bugs).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, bugs)
}

// CreateBug 创建Bug（tester/tester_lead）
func (h *BugHandler) CreateBug(c *gin.Context) {
	var req dto.CreateBugRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	userID := c.GetUint("userID")

	bug := models.Bug{
		Title:       req.Title,
		Description: req.Description,
		Severity:    req.Severity,
		Status:      models.BugAssigned, // 创建即指派
		TaskID:      req.TaskID,
		CreatorID:   userID,
		AssigneeID:  &req.AssigneeID,
	}

	if err := db.DB.Create(&bug).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// 加载关联数据用于通知
	db.DB.Preload("Task").Preload("Creator").Preload("Assignee").First(&bug, bug.ID)

	// 通知被指派开发
	if bug.AssigneeID != nil {
		var operator models.User
		if err := db.DB.First(&operator, userID).Error; err == nil {
			var target models.User
			if err := db.DB.First(&target, *bug.AssigneeID).Error; err == nil {
				h.notifier.EmitBugEvent(&bug, models.BugNew, models.BugAssigned, &operator, []models.User{target}, "")
			}
		}
	}

	c.JSON(http.StatusCreated, bug)
}

// GetBug 获取Bug详情
func (h *BugHandler) GetBug(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid bug id"})
		return
	}

	var bug models.Bug
	if err := db.DB.Preload("Task").Preload("Creator").Preload("Assignee").First(&bug, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "bug not found"})
		return
	}

	c.JSON(http.StatusOK, bug)
}

// UpdateBug 更新Bug
func (h *BugHandler) UpdateBug(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid bug id"})
		return
	}

	var req dto.UpdateBugRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	var bug models.Bug
	if err := db.DB.First(&bug, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "bug not found"})
		return
	}

	if req.Title != "" {
		bug.Title = req.Title
	}
	if req.Description != "" {
		bug.Description = req.Description
	}
	if req.Severity != "" {
		bug.Severity = req.Severity
	}
	if req.TaskID != 0 {
		bug.TaskID = req.TaskID
	}
	if req.AssigneeID != nil {
		bug.AssigneeID = req.AssigneeID
	}
	if req.FixComment != "" {
		bug.FixComment = req.FixComment
	}
	if req.ReopenReason != "" {
		bug.ReopenReason = req.ReopenReason
	}

	if err := db.DB.Save(&bug).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, bug)
}

// ChangeBugStatusHandler Bug状态变更接口
func (h *BugHandler) ChangeBugStatusHandler(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid bug id"})
		return
	}

	var req dto.ChangeBugStatusRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if req.NewStatus == models.BugReopened && req.Comment == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "reopen reason is required"})
		return
	}

	operatorID := c.GetUint("userID")

	if err := h.ChangeBugStatus(uint(id), req.NewStatus, operatorID, req.Comment); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 保存 reopen_reason
	if req.NewStatus == models.BugReopened {
		db.DB.Model(&models.Bug{}).Where("id = ?", id).Update("reopen_reason", req.Comment)
	}

	c.JSON(http.StatusOK, gin.H{"message": "status changed successfully"})
}

// GetBugHistory 获取Bug状态历史
func (h *BugHandler) GetBugHistory(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid bug id"})
		return
	}

	var histories []models.BugStatusHistory
	if err := db.DB.Where("bug_id = ?", id).Preload("User").Order("changed_at DESC").Find(&histories).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, histories)
}
