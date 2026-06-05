-- 1. 新增 Bug 图片字段
ALTER TABLE bugs
    ADD COLUMN image_path VARCHAR(500) COMMENT 'Bug 图片存储路径',
    ADD COLUMN image_name VARCHAR(255) COMMENT 'Bug 图片原始文件名',
    ADD COLUMN image_size BIGINT      COMMENT 'Bug 图片大小(字节)';

-- 2. fix_comment → remark
ALTER TABLE bugs CHANGE COLUMN fix_comment remark TEXT COMMENT '备注';

-- 3. reopen_reason 数据合并到 remark 后删除
UPDATE bugs
   SET remark = CASE
       WHEN remark IS NULL OR remark = '' THEN reopen_reason
       ELSE CONCAT(remark, '\n[历史重新打开原因] ', reopen_reason)
   END
 WHERE reopen_reason IS NOT NULL AND reopen_reason <> '';
ALTER TABLE bugs DROP COLUMN reopen_reason;

-- 4. 状态值数据迁移
UPDATE bugs SET status = 'unfixed' WHERE status IN ('new','assigned','fixing','reopened');
UPDATE bugs SET status = 'fixed'   WHERE status = 'pending_verify';

-- 5. 调整默认值
ALTER TABLE bugs MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'unfixed';

-- 6. 清空旧 bug 工作流规则
DELETE FROM workflow_rules WHERE rule_type = 'bug';

-- 7. 插入新 bug 工作流规则
INSERT INTO workflow_rules (rule_type, role, from_status, to_status) VALUES
('bug', 'tester',    'fixed',     'unfixed'),
('bug', 'tester',    'fixed',     'closed'),
('bug', 'tester',    'not_a_bug', 'unfixed'),
('bug', 'tester',    'not_a_bug', 'closed'),
('bug', 'dev',       'unfixed',   'fixed'),
('bug', 'dev',       'unfixed',   'not_a_bug'),
('bug', 'dev_lead',  'unfixed',   'fixed'),
('bug', 'dev_lead',  'unfixed',   'not_a_bug');
