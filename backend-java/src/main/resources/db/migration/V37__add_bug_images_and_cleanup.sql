-- =============================================
-- V37: 新增 bug_images 表（多图支持），清理废弃字段
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

-- 2. 将 bugs 表中已有的单图数据迁移到 bug_images
INSERT INTO bug_images (bug_id, image_path, image_name, image_size, created_at)
SELECT id, image_path, image_name, image_size, COALESCE(updated_at, created_at, NOW())
FROM bugs
WHERE image_path IS NOT NULL AND image_path <> '';

-- 3. 删除 bugs 表中旧的单图字段
ALTER TABLE bugs
    DROP COLUMN image_path,
    DROP COLUMN image_name,
    DROP COLUMN image_size;

-- 4. 删除 bugs 表中已废弃的 developer_id 字段
ALTER TABLE bugs DROP COLUMN developer_id;

-- 5. 删除 users 表中已废弃的字段（实体中已移除）
ALTER TABLE users
    DROP COLUMN IF EXISTS wechat_id,
    DROP COLUMN IF EXISTS dingtalk_id,
    DROP COLUMN IF EXISTS feishu_id,
    DROP COLUMN IF EXISTS group_id;

-- 6. 删除 tasks 表中已废弃的 feature_id 字段
ALTER TABLE tasks DROP COLUMN IF EXISTS feature_id;

-- 7. 更新 bug 工作流规则保留原有规则不变
-- （无变更）
