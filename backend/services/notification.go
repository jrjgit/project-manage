package services

import (
	"fmt"

	"management/internal/notifier"
	"management/models"
)

// NotificationService 通知服务
type NotificationService struct {
	emailNotifier   notifier.Notifier
	nanobotNotifier notifier.Notifier
}

// NewNotificationService 创建通知服务
func NewNotificationService(email notifier.Notifier, nanobot notifier.Notifier) *NotificationService {
	return &NotificationService{
		emailNotifier:   email,
		nanobotNotifier: nanobot,
	}
}

// EmitTaskEvent 发射任务状态变更事件并触发通知
func (s *NotificationService) EmitTaskEvent(task *models.Task, oldStatus, newStatus string, operator *models.User, targets []models.User, comment string) {
	msgTemplate := s.buildTaskMessage(task, oldStatus, newStatus, operator, comment)
	s.sendToTargets(operator, targets, msgTemplate)
}

// EmitBugEvent 发射Bug状态变更事件并触发通知
func (s *NotificationService) EmitBugEvent(bug *models.Bug, oldStatus, newStatus string, operator *models.User, targets []models.User, comment string) {
	msgTemplate := s.buildBugMessage(bug, oldStatus, newStatus, operator, comment)
	s.sendToTargets(operator, targets, msgTemplate)
}

// buildTaskMessage 构建任务通知消息
func (s *NotificationService) buildTaskMessage(task *models.Task, oldStatus, newStatus string, operator *models.User, comment string) string {
	statusMap := map[string]string{
		models.TaskPending:      "待分配",
		models.TaskAssignedLead: "已分配",
		models.TaskDeveloping:   "开发中",
		models.TaskDeveloped:    "开发完成",
		models.TaskPendingTest:  "待测试",
		models.TaskTesting:      "测试中",
		models.TaskPassed:       "测试通过",
		models.TaskRejected:     "打回修改",
		models.TaskClosed:       "已关闭",
	}
	oldLabel := statusMap[oldStatus]
	if oldLabel == "" {
		oldLabel = oldStatus
	}
	newLabel := statusMap[newStatus]
	if newLabel == "" {
		newLabel = newStatus
	}

	base := fmt.Sprintf("任务【%s】状态已从【%s】变更为【%s】，操作人：%s", task.Title, oldLabel, newLabel, operator.Name)
	if comment != "" {
		base += fmt.Sprintf("，备注：%s", comment)
	}
	return base + "。请及时处理。"
}

// buildBugMessage 构建Bug通知消息
func (s *NotificationService) buildBugMessage(bug *models.Bug, oldStatus, newStatus string, operator *models.User, comment string) string {
	statusMap := map[string]string{
		models.BugNew:           "新建",
		models.BugAssigned:      "已分配",
		models.BugFixing:        "修复中",
		models.BugFixed:         "已修复",
		models.BugPendingVerify: "待验证",
		models.BugClosed:        "已关闭",
		models.BugReopened:      "重新打开",
	}
	oldLabel := statusMap[oldStatus]
	if oldLabel == "" {
		oldLabel = oldStatus
	}
	newLabel := statusMap[newStatus]
	if newLabel == "" {
		newLabel = newStatus
	}

	base := fmt.Sprintf("Bug【%s】状态已从【%s】变更为【%s】，操作人：%s", bug.Title, oldLabel, newLabel, operator.Name)
	if comment != "" {
		base += fmt.Sprintf("，备注：%s", comment)
	}
	return base + "。请及时处理。"
}

// sendToTargets 向目标用户发送通知（邮件 + nanobot）
func (s *NotificationService) sendToTargets(operator *models.User, targets []models.User, message string) {
	for _, target := range targets {
		// 跳过自己通知自己
		if operator != nil && operator.ID == target.ID {
			continue
		}

		// 邮件通知
		if s.emailNotifier != nil && target.Email != "" {
			_ = s.emailNotifier.Notify(target.Email, message)
		}

		// nanobot 通知（通过群聊@个人）
		if s.nanobotNotifier != nil && target.Name != "" {
			_ = s.nanobotNotifier.Notify(target.Name, message)
		}
	}
}
