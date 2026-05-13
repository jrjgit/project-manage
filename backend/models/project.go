package models

import "time"

// Project 项目模型
type Project struct {
	ID        uint      `json:"id" gorm:"primaryKey"`
	Name      string    `json:"name" gorm:"not null"`
	PMID      uint      `json:"pm_id" gorm:"not null;index"`
	PM        User      `json:"pm,omitempty" gorm:"foreignKey:PMID"`
	CreatedAt time.Time `json:"created_at"`
}
