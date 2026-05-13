package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"management/db"
	"management/dto"
	"management/models"
)

// GroupHandler 小组Handler
type GroupHandler struct{}

// NewGroupHandler 创建GroupHandler
func NewGroupHandler() *GroupHandler {
	return &GroupHandler{}
}

// ListGroups 列出所有组（含组长和成员计数）
func (h *GroupHandler) ListGroups(c *gin.Context) {
	var groups []models.Group
	if err := db.DB.Preload("DevLead").Find(&groups).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	type GroupWithCount struct {
		models.Group
		MemberCount int64 `json:"member_count"`
	}

	result := make([]GroupWithCount, 0)
	for _, g := range groups {
		var count int64
		db.DB.Model(&models.User{}).Where("group_id = ? AND role = ?", g.ID, models.RoleDev).Count(&count)
		result = append(result, GroupWithCount{
			Group:       g,
			MemberCount: count,
		})
	}

	c.JSON(http.StatusOK, result)
}

// CreateGroup 创建组（仅PM）
func (h *GroupHandler) CreateGroup(c *gin.Context) {
	var req dto.CreateGroupRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	userID := c.GetUint("userID")

	group := models.Group{
		Name:      req.Name,
		PMID:      userID,
		DevLeadID: req.DevLeadID,
	}

	if err := db.DB.Create(&group).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// 将组长的 group_id 设为此组
	db.DB.Model(&models.User{}).Where("id = ?", req.DevLeadID).Update("group_id", group.ID)

	c.JSON(http.StatusCreated, group)
}

// GetGroup 获取组详情 + 成员列表
func (h *GroupHandler) GetGroup(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	var group models.Group
	if err := db.DB.Preload("DevLead").First(&group, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "group not found"})
		return
	}

	var members []models.User
	db.DB.Where("group_id = ?", id).Find(&members)

	c.JSON(http.StatusOK, gin.H{
		"group":   group,
		"members": members,
	})
}

// UpdateGroup 编辑组信息（仅PM）
func (h *GroupHandler) UpdateGroup(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	var req dto.UpdateGroupRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	updates := map[string]interface{}{
		"name":        req.Name,
		"dev_lead_id": req.DevLeadID,
	}

	// 如果更换了组长，更新新旧组长的 group_id
	var oldGroup models.Group
	if err := db.DB.First(&oldGroup, id).Error; err == nil {
		if oldGroup.DevLeadID != req.DevLeadID {
			db.DB.Model(&models.User{}).Where("id = ?", oldGroup.DevLeadID).Update("group_id", nil)
			db.DB.Model(&models.User{}).Where("id = ?", req.DevLeadID).Update("group_id", uint(id))
		}
	}

	if err := db.DB.Model(&models.Group{}).Where("id = ?", id).Updates(updates).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "group updated"})
}

// DeleteGroup 删除组（清空成员 group_id）
func (h *GroupHandler) DeleteGroup(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	// 清空所有成员的 group_id
	db.DB.Model(&models.User{}).Where("group_id = ?", id).Update("group_id", nil)

	if err := db.DB.Delete(&models.Group{}, id).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "group deleted"})
}

// AddMember 添加成员
func (h *GroupHandler) AddMember(c *gin.Context) {
	groupID, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	var req dto.AddMemberRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 验证组存在
	if err := db.DB.First(&models.Group{}, groupID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "group not found"})
		return
	}

	// 验证用户存在
	var user models.User
	if err := db.DB.First(&user, req.UserID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
		return
	}

	// 设置用户的 group_id
	if err := db.DB.Model(&user).Update("group_id", uint(groupID)).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "member added"})
}

// RemoveMember 移除成员
func (h *GroupHandler) RemoveMember(c *gin.Context) {
	groupID, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	userID, err := strconv.ParseUint(c.Param("user_id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid user id"})
		return
	}

	// 清空该用户的 group_id（仅当该用户确实在此组中）
	if err := db.DB.Model(&models.User{}).Where("id = ? AND group_id = ?", userID, uint(groupID)).Update("group_id", nil).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "member removed"})
}

// GetMyTeam 开发组长查看自己的组员
func (h *GroupHandler) GetMyTeam(c *gin.Context) {
	userID := c.GetUint("userID")

	// 查找当前用户作为组长的 Group
	var group models.Group
	if err := db.DB.Where("dev_lead_id = ?", userID).First(&group).Error; err != nil {
		c.JSON(http.StatusOK, gin.H{
			"group":   nil,
			"members": []models.User{},
		})
		return
	}

	var members []models.User
	db.DB.Where("group_id = ?", group.ID).Find(&members)

	c.JSON(http.StatusOK, gin.H{
		"group":   group,
		"members": members,
	})
}
