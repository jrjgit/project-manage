package notifier

import "log"

// LogNotifier 日志通知器，用于开发调试，只打印不真正发送
type LogNotifier struct{}

// NewLogNotifier 创建日志通知器
func NewLogNotifier() *LogNotifier {
	return &LogNotifier{}
}

// Notify 打印通知日志
func (l *LogNotifier) Notify(userID string, message string) error {
	log.Printf("[NOTIFY] to=%s msg=%s", userID, message)
	return nil
}
