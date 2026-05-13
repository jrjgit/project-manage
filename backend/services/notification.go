package services

import (
	"fmt"
	"log"
	"os/exec"

	"management/config"
	"management/models"
)

// NotificationService 通知服务
type NotificationService struct {
	cfg *config.Config
}

// NewNotificationService 创建通知服务
func NewNotificationService(cfg *config.Config) *NotificationService {
	return &NotificationService{cfg: cfg}
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

// sendToTargets 向目标用户发送通知（只发企业微信）
func (s *NotificationService) sendToTargets(operator *models.User, targets []models.User, message string) {
	for _, target := range targets {
		// 跳过自己通知自己
		if operator != nil && operator.ID == target.ID {
			continue
		}
		// 只发企业微信
		if target.WechatID == "" {
			continue
		}

		instruction := fmt.Sprintf("请通过企业微信给 @%s 发送消息：%s", target.Name, message)
		s.executeNanobot(instruction)
	}
}

// executeNanobot 执行nanobot命令（当前为日志模拟）
func (s *NotificationService) executeNanobot(instruction string) {
	// 日志模拟模式
	log.Printf("[NOTIFY] %s", instruction)

	// 真实调用（解除下面注释即可启用）
	// cmd := exec.Command(s.cfg.NanobotPath, "agent", "-m", instruction)
	// if err := cmd.Run(); err != nil {
	//     log.Printf("[NOTIFY ERROR] failed to execute nanobot: %v", err)
	// }
}

// executeNanobotReal 实际调用nanobot（预留）
func (s *NotificationService) executeNanobotReal(instruction string) {
	cmd := exec.Command(s.cfg.NanobotPath, "agent", "-m", instruction)
	if err := cmd.Run(); err != nil {
		log.Printf("[NOTIFY ERROR] failed to execute nanobot: %v", err)
	}
}
