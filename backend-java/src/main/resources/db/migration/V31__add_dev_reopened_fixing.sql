INSERT INTO workflow_rules (rule_type, role, from_status, to_status)
SELECT 'bug', 'dev', 'reopened', 'fixing'
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_rules
    WHERE rule_type='bug' AND role='dev' AND from_status='reopened' AND to_status='fixing'
);
