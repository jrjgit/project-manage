-- 为 bugs 表中缺失 requirement_id 的记录从关联任务回填
UPDATE bugs b
INNER JOIN tasks t ON b.task_id = t.id
SET b.requirement_id = t.requirement_id
WHERE b.requirement_id IS NULL AND t.requirement_id IS NOT NULL;
