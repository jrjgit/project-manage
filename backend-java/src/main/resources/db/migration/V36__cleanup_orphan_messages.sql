-- 清理 site_messages 表中关联已删除业务数据的记录
-- 删除关联任务已不存在的消息
DELETE FROM site_messages WHERE type = 'task' AND related_id IS NOT NULL AND related_id NOT IN (SELECT id FROM tasks);

-- 删除关联 Bug 已不存在的消息
DELETE FROM site_messages WHERE type = 'bug' AND related_id IS NOT NULL AND related_id NOT IN (SELECT id FROM bugs);

-- 删除关联需求已不存在的消息
DELETE FROM site_messages WHERE type = 'requirement' AND related_id IS NOT NULL AND related_id NOT IN (SELECT id FROM requirements);
