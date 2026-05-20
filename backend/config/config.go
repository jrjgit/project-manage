package config

import (
	"os"
	"strconv"
)

// Config 应用配置
type Config struct {
	JWTSecret   string
	DBPath      string
	NanobotPath string
	Port        string

	// SMTP 邮件配置
	SMTPHost     string
	SMTPPort     string
	SMTPUser     string
	SMTPPassword string
	SMTPFrom     string

	// 是否启用邮件通知
	NotifyEmailEnable bool
	// 是否启用 nanobot 通知
	NotifyNanobotEnable bool
	// 是否异步发送通知
	NotifyAsync bool
}

// Load 从环境变量加载配置，未设置时使用默认值
func Load() *Config {
	return &Config{
		JWTSecret:   getEnv("JWT_SECRET", "your-secret-key-change-in-production"),
		DBPath:      getEnv("DB_PATH", "./management.db"),
		NanobotPath: getEnv("NANOBOT_PATH", "nanobot"),
		Port:        getEnv("PORT", "8080"),

		SMTPHost:     getEnv("SMTP_HOST", ""),
		SMTPPort:     getEnv("SMTP_PORT", "587"),
		SMTPUser:     getEnv("SMTP_USER", ""),
		SMTPPassword: getEnv("SMTP_PASSWORD", ""),
		SMTPFrom:     getEnv("SMTP_FROM", ""),

		NotifyEmailEnable:   getEnv("NOTIFY_EMAIL_ENABLE", "false") == "true",
		NotifyNanobotEnable: getEnv("NOTIFY_NANOBOT_ENABLE", "true") == "true",
		NotifyAsync:         getEnv("NOTIFY_ASYNC", "true") == "true",
	}
}

func getEnv(key, defaultVal string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return defaultVal
}

func parseUint(s string) uint {
	v, _ := strconv.ParseUint(s, 10, 64)
	return uint(v)
}
