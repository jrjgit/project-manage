package notifier

import (
	"fmt"
	"log"
	"os/exec"

	"management/config"
)

// NanobotNotifier nanobot 命令行通知器（通过群聊@个人）
type NanobotNotifier struct {
	path string
}

// NewNanobotNotifier 创建 nanobot 通知器
func NewNanobotNotifier(cfg *config.Config) *NanobotNotifier {
	return &NanobotNotifier{path: cfg.NanobotPath}
}

// Notify 通过 nanobot 给指定用户发送消息
// userID 为用户显示名，用于在群聊中 @对方
func (n *NanobotNotifier) Notify(userID string, message string) error {
	if n.path == "" {
		return fmt.Errorf("nanobot path not configured")
	}
	if userID == "" {
		return nil
	}

	instruction := fmt.Sprintf("给%s发送消息：%s", userID, message)
	cmd := exec.Command(n.path, "agent", "-m", instruction)
	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Printf("[NANOBOT ERROR] to=%s err=%v output=%s", userID, err, string(out))
		return fmt.Errorf("nanobot execution failed: %w", err)
	}

	log.Printf("[NANOBOT] to=%s output=%s", userID, string(out))
	return nil
}
