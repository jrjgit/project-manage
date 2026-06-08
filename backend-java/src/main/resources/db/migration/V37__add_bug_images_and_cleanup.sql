-- =============================================
-- V37: 新增 bug_images 表（多图支持），清理废弃字段
-- 说明：使用动态 SQL，不依赖 DELIMITER / 存储过程，兼容所有 MySQL 客户端
-- =============================================

-- 1. 新增 bug_images 表
CREATE TABLE IF NOT EXISTS bug_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    bug_id      BIGINT       NOT NULL COMMENT '关联Bug ID',
    image_path  VARCHAR(500) NOT NULL COMMENT '图片存储路径',
    image_name  VARCHAR(255) COMMENT '原始文件名',
    image_size  BIGINT       COMMENT '文件大小(字节)',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_bug_id (bug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bug截图表';

-- 2. 迁移 bugs 表中的单图数据（仅当 image_path 列存在时）
SET @has_img = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bugs' AND COLUMN_NAME = 'image_path');
SET @migrate = IF(@has_img > 0,
    'INSERT INTO bug_images (bug_id, image_path, image_name, image_size, created_at) SELECT id, image_path, image_name, image_size, COALESCE(updated_at, created_at, NOW()) FROM bugs WHERE image_path IS NOT NULL AND LENGTH(image_path) > 0',
    'SELECT 1');
PREPARE stmt FROM @migrate;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 删除 bugs 表中旧字段：image_path / image_name / image_size
SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bugs' AND COLUMN_NAME = 'image_path');
SET @sql = IF(@has > 0, 'ALTER TABLE bugs DROP COLUMN image_path', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bugs' AND COLUMN_NAME = 'image_name');
SET @sql = IF(@has > 0, 'ALTER TABLE bugs DROP COLUMN image_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bugs' AND COLUMN_NAME = 'image_size');
SET @sql = IF(@has > 0, 'ALTER TABLE bugs DROP COLUMN image_size', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. 删除 bugs.developer_id
SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bugs' AND COLUMN_NAME = 'developer_id');
SET @sql = IF(@has > 0, 'ALTER TABLE bugs DROP COLUMN developer_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5. 删除 users 废弃字段
SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'wechat_id');
SET @sql = IF(@has > 0, 'ALTER TABLE users DROP COLUMN wechat_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'dingtalk_id');
SET @sql = IF(@has > 0, 'ALTER TABLE users DROP COLUMN dingtalk_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'feishu_id');
SET @sql = IF(@has > 0, 'ALTER TABLE users DROP COLUMN feishu_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'group_id');
SET @sql = IF(@has > 0, 'ALTER TABLE users DROP COLUMN group_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6. 删除 tasks.feature_id
SET @has = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tasks' AND COLUMN_NAME = 'feature_id');
SET @sql = IF(@has > 0, 'ALTER TABLE tasks DROP COLUMN feature_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
