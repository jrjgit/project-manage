-- =============================================
-- V39: 将 tasks.title 改为 TEXT，支持超长任务标题
-- =============================================

ALTER TABLE tasks MODIFY COLUMN title TEXT NOT NULL COMMENT '任务标题';
