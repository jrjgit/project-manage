package models

import "time"

// TaskAssignee 任务开发人员关联表
// 支持一个任务分配多名开发，每人独立跟踪开发状态
type TaskAssignee struct {
	ID        uint      `json:"id" gorm:"primaryKey"`
	TaskID    uint      `json:"task_id" gorm:"not null;index"`
	UserID    uint      `json:"user_id" gorm:"not null;index"`
	User      User      `json:"user,omitempty" gorm:"foreignKey:UserID"`
	Platform  string    `json:"platform"` // iOS / Android / 后台 / 前端 / 其他
	Status    string    `json:"status" gorm:"default:'pending'"` // pending / developing / developed
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}
