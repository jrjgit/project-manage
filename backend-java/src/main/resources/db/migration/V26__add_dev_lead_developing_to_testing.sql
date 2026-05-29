INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'dev_lead', 'developing', 'testing'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules
    WHERE rule_type='task' AND role='dev_lead' AND from_status='developing' AND to_status='testing'
);
