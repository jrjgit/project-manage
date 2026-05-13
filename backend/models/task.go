package models

import "time"

// 任务状态常量
const (
	TaskPending       = "pending"
	TaskAssignedLead  = "assigned_lead"
	TaskDeveloping    = "developing"
	TaskDeveloped     = "developed"
	TaskPendingTest   = "pending_test"
	TaskTesting       = "testing"
	TaskPassed        = "passed"
	TaskRejected      = "rejected"
	TaskClosed        = "closed"
)

// Task 任务模型
type Task struct {
	ID           uint      `json:"id" gorm:"primaryKey"`
	Title        string    `json:"title" gorm:"not null"`
	Description  string    `json:"description"`
	Status       string    `json:"status" gorm:"not null;index;default:'pending'"`
	Priority     string    `json:"priority" gorm:"default:'medium'"`
	ProjectID    uint      `json:"project_id" gorm:"index"`
	Project      Project   `json:"project,omitempty" gorm:"foreignKey:ProjectID"`
	CreatorID    uint      `json:"creator_id" gorm:"not null;index"`
	Creator      User      `json:"creator,omitempty" gorm:"foreignKey:CreatorID"`
	AssigneeID   *uint     `json:"assignee_id" gorm:"index"`
	Assignee     *User     `json:"assignee,omitempty" gorm:"foreignKey:AssigneeID"`
	DevLeadID    *uint     `json:"dev_lead_id" gorm:"index"`
	DevLead      *User     `json:"dev_lead,omitempty" gorm:"foreignKey:DevLeadID"`
	TesterLeadID *uint     `json:"tester_lead_id" gorm:"index"`
	TesterLead   *User     `json:"tester_lead,omitempty" gorm:"foreignKey:TesterLeadID"`
	TesterID     *uint     `json:"tester_id" gorm:"index"`
	Tester       *User     `json:"tester,omitempty" gorm:"foreignKey:TesterID"`
	RejectReason string    `json:"reject_reason"`
	Deadline     *time.Time `json:"deadline"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}
