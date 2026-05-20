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
	"management/internal/notifier"
	"management/models"
	"management/services"
)

// TaskHandler 任务Handler
type TaskHandler struct {
	notifier *services.NotificationService
}

// NewTaskHandler 创建TaskHandler
func NewTaskHandler(cfg *config.Config, email notifier.Notifier, nanobot notifier.Notifier) *TaskHandler {
	return &TaskHandler{
		notifier: services.NewNotificationService(email, nanobot),
	}
}

// ========== 状态机校验 ==========

// isValidTaskTransition 校验任务状态流转是否合法
func isValidTaskTransition(role, from, to string) bool {
	// 定义合法流转映射: 当前状态 -> 允许的目标状态集合
	validTransitions := map[string][]string{
		models.TaskPending:      {models.TaskAssignedLead},
		models.TaskAssignedLead: {models.TaskDeveloping},
		models.TaskDeveloping:   {models.TaskDeveloped},
		models.TaskDeveloped:    {models.TaskPendingTest}, // 系统自动，但也允许手动
		models.TaskPendingTest:  {models.TaskTesting},
		models.TaskTesting:      {models.TaskPassed, models.TaskRejected},
		models.TaskRejected:     {models.TaskDeveloping, models.TaskClosed},
		models.TaskPassed:       {models.TaskClosed},
	}

	allowed, ok := validTransitions[from]
	if !ok {
		return false
	}

	for _, s := range allowed {
		if s == to {
			// 额外角色校验
			if from == models.TaskPending && to == models.TaskAssignedLead && role != models.RolePM {
				return false
			}
			if from == models.TaskAssignedLead && to == models.TaskDeveloping && role != models.RoleDevLead {
				return false
			}
			if from == models.TaskDeveloping && to == models.TaskDeveloped && role != models.RoleDev {
				return false
			}
			if from == models.TaskPendingTest && to == models.TaskTesting && (role != models.RolePM && role != models.RoleTester) {
				return false
			}
			if from == models.TaskTesting && (to == models.TaskPassed || to == models.TaskRejected) && role != models.RoleTester {
				return false
			}
			if from == models.TaskRejected && to == models.TaskDeveloping && role != models.RoleDev {
				return false
			}
			if to == models.TaskClosed && role != models.RolePM {
				return false
			}
			return true
		}
	}
	return false
}

// ========== 统一状态变更入口 ==========

// ChangeTaskStatus 统一任务状态变更入口（事务内完成：校验->更新->历史->通知）
func (h *TaskHandler) ChangeTaskStatus(taskID uint, newStatus string, operatorID uint, comment string) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		// 1. 查询任务
		var task models.Task
		if err := tx.First(&task, taskID).Error; err != nil {
			return err
		}

		// 2. 查询操作人
		var operator models.User
		if err := tx.First(&operator, operatorID).Error; err != nil {
			return err
		}

		// 3. 校验流转合法性
		if !isValidTaskTransition(operator.Role, task.Status, newStatus) {
			return errors.New("invalid status transition")
		}

		oldStatus := task.Status

		// 4. 更新任务状态
		task.Status = newStatus
		task.UpdatedAt = time.Now()
		if err := tx.Save(&task).Error; err != nil {
			return err
		}

		// 5. 写入历史记录
		history := models.TaskStatusHistory{
			TaskID:     taskID,
			FromStatus: oldStatus,
			ToStatus:   newStatus,
			ChangedBy:  operatorID,
			Comment:    comment,
		}
		if err := tx.Create(&history).Error; err != nil {
			return err
		}

		// 6. 确定通知目标用户
		targets := h.resolveTaskNotifyTargets(tx, &task, oldStatus, newStatus)

		// 7. 发送通知
		h.notifier.EmitTaskEvent(&task, oldStatus, newStatus, &operator, targets, comment)

		// 8. 特殊处理：developed -> 自动流转到 pending_test
		if newStatus == models.TaskDeveloped {
			task.Status = models.TaskPendingTest
			task.UpdatedAt = time.Now()
			if err := tx.Save(&task).Error; err != nil {
				return err
			}
			autoHistory := models.TaskStatusHistory{
				TaskID:     taskID,
				FromStatus: models.TaskDeveloped,
				ToStatus:   models.TaskPendingTest,
				ChangedBy:  0, // 系统操作
				Comment:    "系统自动将任务加入测试池",
			}
			if err := tx.Create(&autoHistory).Error; err != nil {
				return err
			}
			// 通知测试组长
			if task.TesterLeadID != nil {
				var testerLead models.User
				if err := tx.First(&testerLead, *task.TesterLeadID).Error; err == nil {
					h.notifier.EmitTaskEvent(&task, models.TaskDeveloped, models.TaskPendingTest, nil, []models.User{testerLead}, "任务已加入测试池")
				}
			}
		}

		return nil
	})
}

// resolveTaskNotifyTargets 根据状态流转确定通知目标
func (h *TaskHandler) resolveTaskNotifyTargets(tx *gorm.DB, task *models.Task, oldStatus, newStatus string) []models.User {
	var targets []models.User

	switch oldStatus + "->" + newStatus {
	case models.TaskPending + "->" + models.TaskAssignedLead:
		// 通知 dev_lead
		if task.DevLeadID != nil {
			var u models.User
			if err := tx.First(&u, *task.DevLeadID).Error; err == nil {
				targets = append(targets, u)
			}
		}

	case models.TaskAssignedLead + "->" + models.TaskDeveloping:
		// 通知被指派开发
		if task.AssigneeID != nil {
			var u models.User
			if err := tx.First(&u, *task.AssigneeID).Error; err == nil {
				targets = append(targets, u)
			}
		}

	case models.TaskDeveloping + "->" + models.TaskDeveloped:
		// 通知 tester_lead（在自动流转到 pending_test 时实际触发）
		if task.TesterLeadID != nil {
			var u models.User
			if err := tx.First(&u, *task.TesterLeadID).Error; err == nil {
				targets = append(targets, u)
			}
		}

	case models.TaskPendingTest + "->" + models.TaskTesting:
		// 通知 tester_lead
		if task.TesterLeadID != nil {
			var u models.User
			if err := tx.First(&u, *task.TesterLeadID).Error; err == nil {
				targets = append(targets, u)
			}
		}
		// 通知 tester（如果已分配）
		if task.TesterID != nil {
			var u models.User
			if err := tx.First(&u, *task.TesterID).Error; err == nil {
				targets = append(targets, u)
			}
		}

	case models.TaskTesting + "->" + models.TaskPassed:
		// 通知 PM
		var project models.Project
		if err := tx.First(&project, task.ProjectID).Error; err == nil {
			var pm models.User
			if err := tx.First(&pm, project.PMID).Error; err == nil {
				targets = append(targets, pm)
			}
		}
		// 可选通知开发和开发组长
		if task.AssigneeID != nil {
			var u models.User
			if err := tx.First(&u, *task.AssigneeID).Error; err == nil {
				targets = append(targets, u)
			}
		}
		if task.DevLeadID != nil {
			var u models.User
			if err := tx.First(&u, *task.DevLeadID).Error; err == nil {
				targets = append(targets, u)
			}
		}

	case models.TaskTesting + "->" + models.TaskRejected:
		// 通知原开发、开发组长
		if task.AssigneeID != nil {
			var u models.User
			if err := tx.First(&u, *task.AssigneeID).Error; err == nil {
				targets = append(targets, u)
			}
		}
		if task.DevLeadID != nil {
			var u models.User
			if err := tx.First(&u, *task.DevLeadID).Error; err == nil {
				targets = append(targets, u)
			}
		}

	case models.TaskRejected + "->" + models.TaskDeveloping:
		// 通知测试人员
		if task.TesterID != nil {
			var u models.User
			if err := tx.First(&u, *task.TesterID).Error; err == nil {
				targets = append(targets, u)
			}
		}
		// 也通知 tester_lead
		if task.TesterLeadID != nil {
			var u models.User
			if err := tx.First(&u, *task.TesterLeadID).Error; err == nil {
				targets = append(targets, u)
			}
		}
	}

	return targets
}

// ========== HTTP Handlers ==========

// ListTasks 获取任务列表（按角色过滤）
func (h *TaskHandler) ListTasks(c *gin.Context) {
	userID := c.GetUint("userID")
	userRole := c.GetString("userRole")
	groupIDVal, _ := c.Get("groupID")
	var groupID *uint
	if gid, ok := groupIDVal.(*uint); ok && gid != nil {
		groupID = gid
	}

	var tasks []models.Task
	query := db.DB.Model(&models.Task{}).Preload("Project").Preload("Creator").Preload("Assignee").Preload("DevLead").Preload("TesterLead").Preload("Tester").Preload("TaskAssignees.User")

	switch userRole {
	case models.RolePM:
		// PM 查看所有
	case models.RoleDevLead:
		// 开发组长：本组任务（dev_lead_id = 自己 或 assignee 是本组成员）
		if groupID != nil {
			query = query.Where("dev_lead_id = ? OR assignee_id IN (?)", userID,
				db.DB.Model(&models.User{}).Select("id").Where("group_id = ?", *groupID))
		} else {
			query = query.Where("dev_lead_id = ?", userID)
		}
	case models.RoleDev:
		// 开发：看自己被分配到的任务（通过 task_assignees 关联表）
		query = query.Where("id IN (?)", db.DB.Model(&models.TaskAssignee{}).Select("task_id").Where("user_id = ?", userID))
	case models.RoleTesterLead:
		// 测试组长：看测试相关任务
		query = query.Where("tester_lead_id = ? OR status IN ?", userID, []string{models.TaskPendingTest, models.TaskTesting, models.TaskPassed, models.TaskRejected})
	case models.RoleTester:
		// 测试： tester_id = 自己 或 creator_id = 自己
		query = query.Where("tester_id = ? OR creator_id = ?", userID, userID)
	default:
		query = query.Where("1=0") // 无权限
	}

	// 支持按项目、状态、优先级筛选
	if projectID := c.Query("project_id"); projectID != "" {
		query = query.Where("project_id = ?", projectID)
	}
	if status := c.Query("status"); status != "" {
		query = query.Where("status = ?", status)
	}
	if priority := c.Query("priority"); priority != "" {
		query = query.Where("priority = ?", priority)
	}

	if err := query.Order("updated_at DESC").Find(&tasks).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, tasks)
}

// CreateTask 创建任务（仅PM）
func (h *TaskHandler) CreateTask(c *gin.Context) {
	var req dto.CreateTaskRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	userID := c.GetUint("userID")

	var deadline *time.Time
	if req.Deadline != nil {
		d, err := time.Parse("2006-01-02", *req.Deadline)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid deadline format, use YYYY-MM-DD"})
			return
		}
		deadline = &d
	}

	task := models.Task{
		Title:       req.Title,
		Description: req.Description,
		Status:      models.TaskPending,
		Priority:    req.Priority,
		ProjectID:   req.ProjectID,
		CreatorID:   userID,
		DevLeadID:   &req.DevLeadID,
		Deadline:    deadline,
	}
	// 兼容单 assignee
	if req.AssigneeID != nil {
		task.AssigneeID = req.AssigneeID
	}
	if req.TesterID != nil {
		task.TesterID = req.TesterID
	}

	if err := db.DB.Create(&task).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// 优先使用新的 assignees（带 platform）
	if len(req.Assignees) > 0 {
		for _, a := range req.Assignees {
			ta := models.TaskAssignee{
				TaskID:   task.ID,
				UserID:   a.UserID,
				Platform: a.Platform,
				Status:   "pending",
			}
			db.DB.Create(&ta)
		}
		if task.AssigneeID == nil {
			db.DB.Model(&task).Update("assignee_id", req.Assignees[0].UserID)
		}
	} else if len(req.AssigneeIDs) > 0 {
		// 兼容旧格式 assignee_ids
		for _, uid := range req.AssigneeIDs {
			ta := models.TaskAssignee{
				TaskID: task.ID,
				UserID: uid,
				Status: "pending",
			}
			db.DB.Create(&ta)
		}
		if task.AssigneeID == nil {
			db.DB.Model(&task).Update("assignee_id", req.AssigneeIDs[0])
		}
	} else if req.AssigneeID != nil {
		// 如果只传了单个 assignee_id，也创建关联记录
		ta := models.TaskAssignee{
			TaskID: task.ID,
			UserID: *req.AssigneeID,
			Status: "pending",
		}
		db.DB.Create(&ta)
	}

	c.JSON(http.StatusCreated, task)
}

// GetTask 获取任务详情
func (h *TaskHandler) GetTask(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid task id"})
		return
	}

	var task models.Task
	if err := db.DB.Preload("Project").Preload("Creator").Preload("Assignee").Preload("DevLead").Preload("TesterLead").Preload("Tester").First(&task, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "task not found"})
		return
	}

	// 加载多开发关联
	var taskAssignees []models.TaskAssignee
	db.DB.Where("task_id = ?", id).Preload("User").Find(&taskAssignees)
	c.JSON(http.StatusOK, gin.H{
		"task":       task,
		"assignees":  taskAssignees,
	})
}

// UpdateTask 更新任务基本信息
func (h *TaskHandler) UpdateTask(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid task id"})
		return
	}

	var req dto.UpdateTaskRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	var task models.Task
	if err := db.DB.First(&task, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "task not found"})
		return
	}

	// 检测 tester_id 变化，触发通知
	oldTesterID := task.TesterID

	if req.Title != "" {
		task.Title = req.Title
	}
	if req.Description != "" {
		task.Description = req.Description
	}
	if req.Priority != "" {
		task.Priority = req.Priority
	}
	if req.ProjectID != 0 {
		task.ProjectID = req.ProjectID
	}
	if req.DevLeadID != 0 {
		task.DevLeadID = &req.DevLeadID
	}
	if req.AssigneeID != nil {
		task.AssigneeID = req.AssigneeID
	}
	if req.TesterLeadID != nil {
		task.TesterLeadID = req.TesterLeadID
	}
	if req.TesterID != nil {
		task.TesterID = req.TesterID
	}
	if req.Deadline != nil {
		d, err := time.Parse("2006-01-02", *req.Deadline)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "invalid deadline format"})
			return
		}
		task.Deadline = &d
	}

	if err := db.DB.Save(&task).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// 如果 tester_id 变化了，通知新 tester
	if req.TesterID != nil && oldTesterID != nil && *req.TesterID != *oldTesterID {
		var newTester models.User
		if err := db.DB.First(&newTester, *req.TesterID).Error; err == nil {
			operatorID := c.GetUint("userID")
			var operator models.User
			if err := db.DB.First(&operator, operatorID).Error; err == nil {
				h.notifier.EmitTaskEvent(&task, task.Status, task.Status, &operator, []models.User{newTester}, "您被分配为测试人员")
			}
		}
	}

	c.JSON(http.StatusOK, task)
}

// ChangeTaskStatusHandler 任务状态变更接口
func (h *TaskHandler) ChangeTaskStatusHandler(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid task id"})
		return
	}

	var req dto.ChangeTaskStatusRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// rejected 状态必须有原因
	if req.NewStatus == models.TaskRejected && req.Comment == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "reject reason is required"})
		return
	}

	operatorID := c.GetUint("userID")
	userRole := c.GetString("userRole")

	// 获取当前任务状态
	var currentTask models.Task
	db.DB.First(&currentTask, id)

	// 多开发场景：dev 开始开发时（任务已在 developing），只更新自己的 TaskAssignee 状态
	if req.NewStatus == models.TaskDeveloping && userRole == models.RoleDev && currentTask.Status == models.TaskDeveloping {
		db.DB.Model(&models.TaskAssignee{}).Where("task_id = ? AND user_id = ?", id, operatorID).Update("status", "developing")
		c.JSON(http.StatusOK, gin.H{"message": "started developing"})
		return
	}

	// 多开发场景：dev 标记 developed 时，先更新自己的 TaskAssignee
	if req.NewStatus == models.TaskDeveloped && userRole == models.RoleDev {
		var taskAssignees []models.TaskAssignee
		db.DB.Where("task_id = ?", id).Find(&taskAssignees)
		if len(taskAssignees) > 0 {
			// 更新自己的 TaskAssignee 状态
			db.DB.Model(&models.TaskAssignee{}).Where("task_id = ? AND user_id = ?", id, operatorID).Update("status", "developed")
			// 检查是否全部完成
			var pendingCount int64
			db.DB.Model(&models.TaskAssignee{}).Where("task_id = ? AND status != ?", id, "developed").Count(&pendingCount)
			if pendingCount > 0 {
				c.JSON(http.StatusOK, gin.H{"message": "development completed, waiting for other developers"})
				return
			}
			// 全部完成，继续流转任务状态
		}
	}

	// 多开发场景：dev 开始开发时（任务从 assigned_lead -> developing），更新自己的 TaskAssignee 状态
	if req.NewStatus == models.TaskDeveloping && userRole == models.RoleDev {
		db.DB.Model(&models.TaskAssignee{}).Where("task_id = ? AND user_id = ?", id, operatorID).Update("status", "developing")
	}

	if err := h.ChangeTaskStatus(uint(id), req.NewStatus, operatorID, req.Comment); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 如果是 rejected，保存原因到任务
	if req.NewStatus == models.TaskRejected {
		db.DB.Model(&models.Task{}).Where("id = ?", id).Update("reject_reason", req.Comment)
	}

	c.JSON(http.StatusOK, gin.H{"message": "status changed successfully"})
}

// GetTaskHistory 获取任务状态历史
func (h *TaskHandler) GetTaskHistory(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid task id"})
		return
	}

	var histories []models.TaskStatusHistory
	if err := db.DB.Where("task_id = ?", id).Preload("User").Order("changed_at DESC").Find(&histories).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, histories)
}

// AddTaskAssignee 为任务添加开发人员
func (h *TaskHandler) AddTaskAssignee(c *gin.Context) {
	taskID, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid task id"})
		return
	}

	var req dto.AddTaskAssigneeRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 检查是否已存在
	var existing models.TaskAssignee
	if db.DB.Where("task_id = ? AND user_id = ?", taskID, req.UserID).First(&existing).Error == nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "user already assigned to this task"})
		return
	}

	ta := models.TaskAssignee{
		TaskID:   uint(taskID),
		UserID:   req.UserID,
		Platform: req.Platform,
		Status:   "pending",
	}
	if err := db.DB.Create(&ta).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// 如果没有主 assignee，设为第一个
	var task models.Task
	if db.DB.First(&task, taskID).Error == nil && task.AssigneeID == nil {
		db.DB.Model(&task).Update("assignee_id", req.UserID)
	}

	c.JSON(http.StatusCreated, ta)
}

// RemoveTaskAssignee 移除任务的开发人员
func (h *TaskHandler) RemoveTaskAssignee(c *gin.Context) {
	taskID, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid task id"})
		return
	}

	userID, err := strconv.ParseUint(c.Param("user_id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid user id"})
		return
	}

	if err := db.DB.Where("task_id = ? AND user_id = ?", taskID, userID).Delete(&models.TaskAssignee{}).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "assignee removed"})
}
