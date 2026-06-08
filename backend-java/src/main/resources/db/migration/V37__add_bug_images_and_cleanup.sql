-- =============================================
-- V37: 新增 bug_images 表（多图支持），清理废弃字段
-- =============================================

-- 0. 创建辅助存储过程：删除表中指定列（如果存在）
DROP PROCEDURE IF EXISTS drop_col_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_col_if_exists(IN tbl VARCHAR(255), IN col VARCHAR(255))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl, ' DROP COLUMN ', col);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

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

-- 2. 将 bugs 表中已有的单图数据迁移到 bug_images
INSERT INTO bug_images (bug_id, image_path, image_name, image_size, created_at)
SELECT id, image_path, image_name, image_size, COALESCE(updated_at, created_at, NOW())
FROM bugs
WHERE image_path IS NOT NULL AND image_path <> '';

-- 3. 删除 bugs 表中旧的单图字段（必须先确认存在，否则多字段 ALTER 会整体失败）
CALL drop_col_if_exists('bugs', 'image_path');
CALL drop_col_if_exists('bugs', 'image_name');
CALL drop_col_if_exists('bugs', 'image_size');

-- 4. 删除 bugs 表中已废弃的 developer_id 字段
CALL drop_col_if_exists('bugs', 'developer_id');

-- 5. 删除 users 表中已废弃的字段
CALL drop_col_if_exists('users', 'wechat_id');
CALL drop_col_if_exists('users', 'dingtalk_id');
CALL drop_col_if_exists('users', 'feishu_id');
CALL drop_col_if_exists('users', 'group_id');

-- 6. 删除 tasks 表中已废弃的 feature_id 字段
CALL drop_col_if_exists('tasks', 'feature_id');

-- 7. 清理辅助存储过程
DROP PROCEDURE IF EXISTS drop_col_if_exists;
