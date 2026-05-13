package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"management/db"
	"management/models"
)

// UserHandler 用户Handler
type UserHandler struct{}

// NewUserHandler 创建UserHandler
func NewUserHandler() *UserHandler {
	return &UserHandler{}
}

// ListUsers 获取用户列表
func (h *UserHandler) ListUsers(c *gin.Context) {
	userRole := c.GetString("userRole")
	var users []models.User
	query := db.DB.Model(&models.User{})

	// tester/tester_lead 只能查看开发人员列表（用于指派修复人）
	if userRole == models.RoleTester || userRole == models.RoleTesterLead {
		query = query.Where("role = ?", models.RoleDev)
	}

	if err := query.Find(&users).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, users)
}

// GetUser 获取单个用户
func (h *UserHandler) GetUser(c *gin.Context) {
	id := c.Param("id")
	var user models.User
	if err := db.DB.First(&user, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
		return
	}
	c.JSON(http.StatusOK, user)
}

// UpdateUserRole 更新用户角色（仅PM）
func (h *UserHandler) UpdateUserRole(c *gin.Context) {
	id := c.Param("id")
	var req struct {
		Role    string `json:"role" binding:"required,oneof=pm dev_lead dev tester_lead tester"`
		GroupID *uint  `json:"group_id"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	updates := map[string]interface{}{
		"role": req.Role,
	}
	if req.GroupID != nil {
		updates["group_id"] = *req.GroupID
	}

	if err := db.DB.Model(&models.User{}).Where("id = ?", id).Updates(updates).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "user updated"})
}
