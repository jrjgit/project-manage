-- Add task workflow rules for dev/dev_lead to manage testing and close tasks
INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'dev_lead', 'testing', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='task' AND role='dev_lead' AND from_status='testing' AND to_status='closed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'dev_lead', 'testing', 'developing'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='task' AND role='dev_lead' AND from_status='testing' AND to_status='developing'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'dev', 'testing', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='task' AND role='dev' AND from_status='testing' AND to_status='closed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'task', 'dev', 'testing', 'developing'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='task' AND role='dev' AND from_status='testing' AND to_status='developing'
);

-- Add bug workflow rules for dev/dev_lead to verify bugs
INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev', 'fixed', 'unfixed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev' AND from_status='fixed' AND to_status='unfixed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev', 'fixed', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev' AND from_status='fixed' AND to_status='closed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev', 'not_a_bug', 'unfixed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev' AND from_status='not_a_bug' AND to_status='unfixed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev', 'not_a_bug', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev' AND from_status='not_a_bug' AND to_status='closed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev_lead', 'fixed', 'unfixed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev_lead' AND from_status='fixed' AND to_status='unfixed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev_lead', 'fixed', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev_lead' AND from_status='fixed' AND to_status='closed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev_lead', 'not_a_bug', 'unfixed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev_lead' AND from_status='not_a_bug' AND to_status='unfixed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev_lead', 'not_a_bug', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='dev_lead' AND from_status='not_a_bug' AND to_status='closed'
);

-- Add bug workflow rules for pm to verify bugs
INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'pm', 'fixed', 'unfixed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='pm' AND from_status='fixed' AND to_status='unfixed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'pm', 'fixed', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='pm' AND from_status='fixed' AND to_status='closed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'pm', 'not_a_bug', 'unfixed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='pm' AND from_status='not_a_bug' AND to_status='unfixed'
);

INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'pm', 'not_a_bug', 'closed'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules WHERE rule_type='bug' AND role='pm' AND from_status='not_a_bug' AND to_status='closed'
);
