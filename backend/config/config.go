package config

import (
	"os"
)

// Config 应用配置
type Config struct {
	JWTSecret   string
	DBPath      string
	NanobotPath string
	Port        string
}

// Load 从环境变量加载配置，未设置时使用默认值
func Load() *Config {
	return &Config{
		JWTSecret:   getEnv("JWT_SECRET", "your-secret-key-change-in-production"),
		DBPath:      getEnv("DB_PATH", "./management.db"),
		NanobotPath: getEnv("NANOBOT_PATH", "nanobot"),
		Port:        getEnv("PORT", "8080"),
	}
}

func getEnv(key, defaultVal string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return defaultVal
}
