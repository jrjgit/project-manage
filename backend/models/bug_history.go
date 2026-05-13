package models

import "time"

// BugStatusHistory Bug状态变更历史
type BugStatusHistory struct {
	ID         uint      `json:"id" gorm:"primaryKey"`
	BugID      uint      `json:"bug_id" gorm:"not null;index"`
	FromStatus string    `json:"from_status"`
	ToStatus   string    `json:"to_status" gorm:"not null"`
	ChangedBy  uint      `json:"changed_by" gorm:"not null;index"`
	User       User      `json:"user,omitempty" gorm:"foreignKey:ChangedBy"`
	ChangedAt  time.Time `json:"changed_at" gorm:"autoCreateTime"`
	Comment    string    `json:"comment"`
}
