-- =============================================
-- V38: 为Bug表添加预期结果字段
-- =============================================

ALTER TABLE bugs ADD COLUMN expected_result TEXT COMMENT '预期结果';

-- 将旧的 description 内容同步到 expected_result（可选，方便已有数据）
UPDATE bugs SET expected_result = description WHERE expected_result IS NULL AND description IS NOT NULL;
