package config

import (
	"bufio"
	"os"
	"strings"
)

// LoadEnvFile 从 .env 文件加载环境变量，不覆盖已存在的系统环境变量
func LoadEnvFile(path string) error {
	f, err := os.Open(path)
	if err != nil {
		if os.IsNotExist(err) {
			return nil // 文件不存在时静默跳过
		}
		return err
	}
	defer f.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}
		parts := strings.SplitN(line, "=", 2)
		if len(parts) != 2 {
			continue
		}
		key := strings.TrimSpace(parts[0])
		val := strings.TrimSpace(parts[1])
		// 去除可能的引号包裹
		val = strings.Trim(val, `"'`)
		if key != "" && os.Getenv(key) == "" {
			os.Setenv(key, val)
		}
	}
	return scanner.Err()
}
