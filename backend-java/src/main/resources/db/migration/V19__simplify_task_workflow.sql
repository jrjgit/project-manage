-- Remove old task workflow rules and insert simplified ones
DELETE FROM workflow_rules WHERE rule_type = 'task';

INSERT INTO workflow_rules (rule_type, role, from_status, to_status) VALUES
('task', 'pm',          'pending',    'developing'),
('task', 'pm',          'pending',    'testing'),
('task', 'pm',          'pending',    'closed'),
('task', 'pm',          'developing', 'testing'),
('task', 'pm',          'developing', 'closed'),
('task', 'pm',          'testing',    'closed'),
('task', 'pm',          'testing',    'developing'),
('task', 'dev_lead',    'pending',    'developing'),
('task', 'dev',         'developing', 'testing'),
('task', 'tester',      'testing',    'closed'),
('task', 'tester',      'testing',    'developing');
