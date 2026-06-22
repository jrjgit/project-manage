-- =============================================
-- 测试用 H2 数据库 Schema
-- 与 init.sql 结构一致（H2 MySQL 兼容模式）
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    account     VARCHAR(255),
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL,
    email       VARCHAR(255),
    skills      VARCHAR(500),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    code         VARCHAR(128),
    project_type VARCHAR(50),
    system_scope TEXT,
    hr_scope     TEXT,
    pm_id        BIGINT,
    created_by   BIGINT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    title            TEXT NOT NULL,
    description      TEXT,
    status           VARCHAR(50)  NOT NULL DEFAULT 'pending',
    priority         VARCHAR(20)  DEFAULT 'medium',
    project_id       BIGINT,
    creator_id       BIGINT       NOT NULL,
    assignee_id      BIGINT,
    dev_lead_id      BIGINT,
    tester_id        BIGINT,
    reject_reason    TEXT,
    deadline         TIMESTAMP,
    requirement_id   BIGINT,
    terminal         VARCHAR(50),
    progress         INT          DEFAULT 0,
    performance      VARCHAR(128),
    test_performance VARCHAR(255),
    iteration_id     VARCHAR(128),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_assignees (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    platform   VARCHAR(100),
    status     VARCHAR(50)  DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_status_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id     BIGINT       NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50)  NOT NULL,
    changed_by  BIGINT       NOT NULL,
    changed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comment     TEXT
);

CREATE TABLE IF NOT EXISTS task_progress_history (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id    BIGINT       NOT NULL,
    progress   INT          NOT NULL,
    comment    VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bugs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    severity       VARCHAR(20)  DEFAULT 'medium',
    status         VARCHAR(50)  NOT NULL DEFAULT 'unfixed',
    task_id        BIGINT       NOT NULL,
    creator_id     BIGINT       NOT NULL,
    assignee_id    BIGINT,
    remark         TEXT,
    requirement_id BIGINT,
    test_type      VARCHAR(20)  DEFAULT 'integration',
    expected_result TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bug_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    bug_id      BIGINT       NOT NULL,
    image_path  VARCHAR(500) NOT NULL,
    image_name  VARCHAR(255),
    image_size  BIGINT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bug_status_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    bug_id      BIGINT       NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50)  NOT NULL,
    changed_by  BIGINT       NOT NULL,
    changed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comment     TEXT
);

CREATE TABLE IF NOT EXISTS workflow_rules (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type   VARCHAR(20)  NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    from_status VARCHAR(30)  NOT NULL,
    to_status   VARCHAR(30)  NOT NULL,
    UNIQUE KEY uk_rule (rule_type, role, from_status, to_status)
);

CREATE TABLE IF NOT EXISTS requirements (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id          VARCHAR(128) NOT NULL,
    number                  VARCHAR(128) NOT NULL,
    title                   VARCHAR(255),
    description             TEXT,
    notes                   TEXT,
    status                  VARCHAR(50)  NOT NULL DEFAULT 'planned',
    priority                VARCHAR(20)  DEFAULT 'medium',
    `system`                VARCHAR(50),
    project_id              BIGINT,
    project_type            VARCHAR(50),
    person_id               BIGINT,
    person_name             VARCHAR(255),
    total_amount            VARCHAR(128),
    total_price             VARCHAR(128),
    dev_total               VARCHAR(128),
    dev_price               VARCHAR(128),
    test_total              VARCHAR(128),
    test_price              VARCHAR(128),
    release_time            TIMESTAMP,
    planned_completion_time TIMESTAMP,
    dev_lead_id             BIGINT,
    business_status         VARCHAR(50),
    iteration_id            VARCHAR(128),
    document_path           VARCHAR(500),
    document_name           VARCHAR(255),
    document_size           BIGINT,
    progress_notes          TEXT,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS iterations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    status       VARCHAR(32)  NOT NULL DEFAULT 'pending_publish',
    release_time TIMESTAMP,
    notes        TEXT,
    release_notes TEXT,
    created_by   BIGINT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS systems (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    it_contact   VARCHAR(255),
    biz_contact  VARCHAR(255),
    tech_contact VARCHAR(255),
    address      TEXT,
    created_by   BIGINT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dictionaries (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type  VARCHAR(50)  NOT NULL,
    dict_key   VARCHAR(100) NOT NULL,
    dict_value VARCHAR(255) NOT NULL,
    sort_order INT          DEFAULT 0
);

CREATE TABLE IF NOT EXISTS site_messages (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    type       VARCHAR(20)  NOT NULL DEFAULT 'system',
    related_id BIGINT,
    is_read    TINYINT      NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at    TIMESTAMP
);
