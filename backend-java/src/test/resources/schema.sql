CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    group_id BIGINT,
    wechat_id VARCHAR(255),
    dingtalk_id VARCHAR(255),
    feishu_id VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(128),
    project_type VARCHAR(50),
    system_scope TEXT,
    hr_scope TEXT,
    pm_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    priority VARCHAR(20) DEFAULT 'medium',
    project_id BIGINT,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT,
    dev_lead_id BIGINT,
    tester_lead_id BIGINT,
    tester_id BIGINT,
    reject_reason TEXT,
    deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_assignees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    platform VARCHAR(100),
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_status_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comment TEXT
);

CREATE TABLE IF NOT EXISTS bugs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(50) NOT NULL DEFAULT 'new',
    task_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT,
    fix_comment TEXT,
    reopen_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bug_status_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bug_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comment TEXT
);

CREATE TABLE IF NOT EXISTS groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pm_id BIGINT NOT NULL,
    dev_lead_id BIGINT NOT NULL,
    lead_role VARCHAR(20) DEFAULT 'dev',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL,
    from_status VARCHAR(30) NOT NULL,
    to_status VARCHAR(30) NOT NULL,
    UNIQUE KEY uk_rule (rule_type, role, from_status, to_status)
);

CREATE TABLE IF NOT EXISTS requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id VARCHAR(128) NOT NULL,
    number VARCHAR(128) NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    source VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'planned',
    priority VARCHAR(20) DEFAULT 'medium',
    system VARCHAR(50),
    project_id BIGINT,
    project_type VARCHAR(50),
    person_id BIGINT,
    person_name VARCHAR(255),
    relevant TEXT,
    total_amount VARCHAR(128),
    dev_total VARCHAR(128),
    dev_price VARCHAR(128),
    test_total VARCHAR(128),
    test_price VARCHAR(128),
    biz_test_total VARCHAR(128),
    biz_test_price VARCHAR(128),
    release_time TIMESTAMP,
    planned_completion_time TIMESTAMP,
    notes TEXT,
    document_path VARCHAR(500),
    document_name VARCHAR(255),
    document_size BIGINT,
    dev_lead_id BIGINT,
    total_price VARCHAR(128),
    iteration_id VARCHAR(128),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'planned',
    developer_id BIGINT,
    tester_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS iterations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iteration_id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    release_time TIMESTAMP,
    notes TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS systems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    it_contact VARCHAR(255),
    biz_contact VARCHAR(255),
    tech_contact VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dictionaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL,
    dict_key VARCHAR(100) NOT NULL,
    dict_value VARCHAR(255) NOT NULL,
    sort_order INT DEFAULT 0
);
