package models

import "time"

// Group 小组模型
type Group struct {
	ID        uint      `json:"id" gorm:"primaryKey"`
	Name      string    `json:"name" gorm:"not null"`
	PMID      uint      `json:"pm_id" gorm:"not null;index"`
	PM        User      `json:"pm,omitempty" gorm:"foreignKey:PMID"`
	DevLeadID uint      `json:"dev_lead_id" gorm:"not null;index"`
	DevLead   User      `json:"dev_lead,omitempty" gorm:"foreignKey:DevLeadID"`
	CreatedAt time.Time `json:"created_at"`
}
