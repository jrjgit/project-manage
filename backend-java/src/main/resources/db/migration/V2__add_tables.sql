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
    relevant TEXT,
    total_amount VARCHAR(128),
    dev_total VARCHAR(128),
    dev_price VARCHAR(128),
    test_total VARCHAR(128),
    test_price VARCHAR(128),
    biz_test_total VARCHAR(128),
    biz_test_price VARCHAR(128),
    release_time DATETIME,
    iteration_id VARCHAR(128),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'planned',
    developer_id BIGINT,
    tester_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS iterations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iteration_id VARCHAR(128),
    name VARCHAR(255) NOT NULL,
    release_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dictionaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL,
    dict_key VARCHAR(100) NOT NULL,
    dict_value VARCHAR(255) NOT NULL,
    sort_order INT DEFAULT 0
);

ALTER TABLE tasks ADD COLUMN IF NOT EXISTS requirement_id BIGINT;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS feature_id BIGINT;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS terminal VARCHAR(50);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS progress INT DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS performance VARCHAR(128);

ALTER TABLE bugs ADD COLUMN IF NOT EXISTS requirement_id BIGINT;
ALTER TABLE bugs ADD COLUMN IF NOT EXISTS developer_id BIGINT;
