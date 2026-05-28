-- Add missing task workflow rules for dev and tester roles
INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'dev', 'pending', 'developing'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='task' AND role='dev' AND from_status='pending' AND to_status='developing'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'tester_lead', 'pending', 'testing'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='task' AND role='tester_lead' AND from_status='pending' AND to_status='testing'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'tester', 'pending', 'testing'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='task' AND role='tester' AND from_status='pending' AND to_status='testing'
);
