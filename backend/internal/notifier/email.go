package notifier

import (
	"fmt"
	"log"
	"net/smtp"

	"management/config"
)

// EmailNotifier SMTP邮件通知器
type EmailNotifier struct {
	host     string
	port     string
	user     string
	password string
	from     string
}

// NewEmailNotifier 创建邮件通知器
func NewEmailNotifier(cfg *config.Config) *EmailNotifier {
	return &EmailNotifier{
		host:     cfg.SMTPHost,
		port:     cfg.SMTPPort,
		user:     cfg.SMTPUser,
		password: cfg.SMTPPassword,
		from:     cfg.SMTPFrom,
	}
}

// Notify 向指定邮箱发送文本邮件
func (e *EmailNotifier) Notify(userID string, message string) error {
	if userID == "" {
		return nil
	}
	if e.host == "" || e.port == "" {
		return fmt.Errorf("smtp not configured")
	}

	addr := e.host + ":" + e.port
	auth := smtp.PlainAuth("", e.user, e.password, e.host)

	subject := "Subject: 【项目管理系统】任务变更通知\r\n"
	contentType := "Content-Type: text/plain; charset=UTF-8\r\n\r\n"
	body := message

	msg := []byte(subject + contentType + body)

	err := smtp.SendMail(addr, auth, e.from, []string{userID}, msg)
	if err != nil {
		return fmt.Errorf("failed to send email: %w", err)
	}

	log.Printf("[EMAIL] sent to %s", userID)
	return nil
}
