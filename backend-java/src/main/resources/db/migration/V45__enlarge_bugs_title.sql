-- =============================================
-- V45: 将 bugs.title 改为 TEXT，支持超长Bug标题
-- =============================================

ALTER TABLE bugs MODIFY COLUMN title TEXT NOT NULL COMMENT 'Bug标题';
