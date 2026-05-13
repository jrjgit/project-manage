package models

import "time"

// Bug 状态常量
const (
	BugNew           = "new"
	BugAssigned      = "assigned"
	BugFixing        = "fixing"
	BugFixed         = "fixed"
	BugPendingVerify = "pending_verify"
	BugClosed        = "closed"
	BugReopened      = "reopened"
)

// Bug 缺陷模型
type Bug struct {
	ID           uint      `json:"id" gorm:"primaryKey"`
	Title        string    `json:"title" gorm:"not null"`
	Description  string    `json:"description"`
	Severity     string    `json:"severity" gorm:"default:'medium'"` // low, medium, high, critical
	Status       string    `json:"status" gorm:"not null;index;default:'new'"`
	TaskID       uint      `json:"task_id" gorm:"not null;index"`
	Task         Task      `json:"task,omitempty" gorm:"foreignKey:TaskID"`
	CreatorID    uint      `json:"creator_id" gorm:"not null;index"`
	Creator      User      `json:"creator,omitempty" gorm:"foreignKey:CreatorID"`
	AssigneeID   *uint     `json:"assignee_id" gorm:"index"`
	Assignee     *User     `json:"assignee,omitempty" gorm:"foreignKey:AssigneeID"`
	FixComment   string    `json:"fix_comment"`
	ReopenReason string    `json:"reopen_reason"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}
