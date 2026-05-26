-- 删除用户时需要将关联字段置空，原 NOT NULL 约束需改为允许 NULL
ALTER TABLE projects MODIFY COLUMN pm_id BIGINT COMMENT '项目经理ID';
ALTER TABLE `groups` MODIFY COLUMN pm_id BIGINT COMMENT '项目经理ID';
ALTER TABLE `groups` MODIFY COLUMN dev_lead_id BIGINT COMMENT '开发组长ID';
