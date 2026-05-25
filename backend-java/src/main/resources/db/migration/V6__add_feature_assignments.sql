-- 1. requirements 表增加开发组长字段
SET @dbname = DATABASE();
SET @exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'requirements' AND COLUMN_NAME = 'dev_lead_id');
SET @sql = IF(@exists = 0,
  'ALTER TABLE requirements ADD COLUMN dev_lead_id BIGINT COMMENT ''开发组长ID''',
  'SELECT ''column dev_lead_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 功能点分配表
CREATE TABLE IF NOT EXISTS feature_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature_id BIGINT NOT NULL COMMENT '关联功能点ID',
    terminal VARCHAR(50) NOT NULL COMMENT '终端: iOS/Android/鸿蒙/小程序/H5/后台/前端/其他',
    developer_id BIGINT NOT NULL COMMENT '开发人员ID',
    status VARCHAR(50) NOT NULL DEFAULT 'pending' COMMENT 'pending/developing/done',
    auto_task_id BIGINT COMMENT '自动生成的任务ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feature (feature_id),
    INDEX idx_developer (developer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能点按端分配表';
