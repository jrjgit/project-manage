package models

import "time"

// TaskStatusHistory 任务状态变更历史
type TaskStatusHistory struct {
	ID        uint      `json:"id" gorm:"primaryKey"`
	TaskID    uint      `json:"task_id" gorm:"not null;index"`
	FromStatus string   `json:"from_status"`
	ToStatus  string    `json:"to_status" gorm:"not null"`
	ChangedBy uint      `json:"changed_by" gorm:"not null;index"`
	User      User      `json:"user,omitempty" gorm:"foreignKey:ChangedBy"`
	ChangedAt time.Time `json:"changed_at" gorm:"autoCreateTime"`
	Comment   string    `json:"comment"`
}
