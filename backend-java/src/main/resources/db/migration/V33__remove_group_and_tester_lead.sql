-- 1. 将 tester_lead 角色的用户转为 tester
UPDATE users SET role = 'tester' WHERE role = 'tester_lead';

-- 2. 删除 workflow_rules 中 tester_lead 的规则
DELETE FROM workflow_rules WHERE role = 'tester_lead';

-- 3. 将 users 表的 group_id 列设为可空（保留列但不再使用，避免外键错误）
ALTER TABLE users MODIFY group_id BIGINT NULL;
