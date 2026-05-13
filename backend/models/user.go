package models

import "time"

// 角色常量
const (
	RolePM         = "pm"
	RoleDevLead    = "dev_lead"
	RoleDev        = "dev"
	RoleTesterLead = "tester_lead"
	RoleTester     = "tester"
)

// User 用户模型
type User struct {
	ID         uint      `json:"id" gorm:"primaryKey"`
	Name       string    `json:"name" gorm:"not null;index"`
	Password   string    `json:"-" gorm:"not null"` // 密码不序列化到JSON
	Role       string    `json:"role" gorm:"not null;index"`
	GroupID    *uint     `json:"group_id" gorm:"index"`
	WechatID   string    `json:"wechat_id"`
	DingtalkID string    `json:"dingtalk_id"`
	FeishuID   string    `json:"feishu_id"`
	Email      string    `json:"email"`
	CreatedAt  time.Time `json:"created_at"`
}
