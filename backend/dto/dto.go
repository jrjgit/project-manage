package dto

import "management/models"

// ========== Auth DTOs ==========

type LoginRequest struct {
	Name     string `json:"name" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type RegisterRequest struct {
	Name     string `json:"name" binding:"required"`
	Password string `json:"password" binding:"required,min=6"`
	Role     string `json:"role" binding:"required,oneof=pm dev_lead dev tester_lead tester"`
	GroupID  *uint  `json:"group_id"`
	WechatID string `json:"wechat_id"`
}

type LoginResponse struct {
	Token string      `json:"token"`
	User  models.User `json:"user"`
}

// ========== Task DTOs ==========

type CreateTaskRequest struct {
	Title       string     `json:"title" binding:"required"`
	Description string     `json:"description"`
	Priority    string     `json:"priority" binding:"oneof=low medium high critical"`
	ProjectID   uint       `json:"project_id" binding:"required"`
	DevLeadID   uint       `json:"dev_lead_id" binding:"required"`
	AssigneeID  *uint      `json:"assignee_id"`
	Deadline    *string    `json:"deadline"`
}

type UpdateTaskRequest struct {
	Title       string  `json:"title"`
	Description string  `json:"description"`
	Priority    string  `json:"priority"`
	ProjectID   uint    `json:"project_id"`
	DevLeadID   uint    `json:"dev_lead_id"`
	AssigneeID  *uint   `json:"assignee_id"`
	TesterLeadID *uint  `json:"tester_lead_id"`
	TesterID    *uint   `json:"tester_id"`
	Deadline    *string `json:"deadline"`
}

type ChangeTaskStatusRequest struct {
	NewStatus string `json:"new_status" binding:"required"`
	Comment   string `json:"comment"`
}

// ========== Bug DTOs ==========

type CreateBugRequest struct {
	Title       string `json:"title" binding:"required"`
	Description string `json:"description"`
	Severity    string `json:"severity" binding:"oneof=low medium high critical"`
	TaskID      uint   `json:"task_id" binding:"required"`
	AssigneeID  uint   `json:"assignee_id" binding:"required"`
}

type UpdateBugRequest struct {
	Title        string `json:"title"`
	Description  string `json:"description"`
	Severity     string `json:"severity"`
	TaskID       uint   `json:"task_id"`
	AssigneeID   *uint  `json:"assignee_id"`
	FixComment   string `json:"fix_comment"`
	ReopenReason string `json:"reopen_reason"`
}

type ChangeBugStatusRequest struct {
	NewStatus string `json:"new_status" binding:"required"`
	Comment   string `json:"comment"`
}

// ========== Project DTOs ==========

type CreateProjectRequest struct {
	Name string `json:"name" binding:"required"`
}

type UpdateProjectRequest struct {
	Name string `json:"name" binding:"required"`
}

// ========== Group DTOs ==========

type CreateGroupRequest struct {
	Name      string `json:"name" binding:"required"`
	DevLeadID uint   `json:"dev_lead_id" binding:"required"`
}

type UpdateGroupRequest struct {
	Name      string `json:"name" binding:"required"`
	DevLeadID uint   `json:"dev_lead_id"`
}

type AddMemberRequest struct {
	UserID uint `json:"user_id" binding:"required"`
}
