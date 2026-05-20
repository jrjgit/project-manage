package db

import (
	"management/config"
	"management/models"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// DB 全局数据库实例
var DB *gorm.DB

// Init 初始化SQLite数据库并自动迁移表结构
func Init(cfg *config.Config) error {
	var err error
	DB, err = gorm.Open(sqlite.Open(cfg.DBPath), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		return err
	}

	return DB.AutoMigrate(
		&models.User{},
		&models.Project{},
		&models.Task{},
		&models.Bug{},
		&models.TaskStatusHistory{},
		&models.BugStatusHistory{},
		&models.Group{},
		&models.TaskAssignee{},
	)
}
