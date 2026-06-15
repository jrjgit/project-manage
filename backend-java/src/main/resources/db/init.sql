-- =============================================
-- 管理系统数据库初始化脚本（基于 V1~V37 迁移最终状态）
-- 适用于 MySQL 8.x
-- 执行此文件即可创建完整表结构及初始数据
-- =============================================

CREATE DATABASE IF NOT EXISTS management
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE management;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    account     VARCHAR(255)          COMMENT '登录账号',
    role        VARCHAR(50)  NOT NULL,
    email       VARCHAR(255),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    skills      VARCHAR(500)          COMMENT '技能列表,逗号分隔: iOS,Android,鸿蒙,小程序,H5,后台,前端,其他',
    PRIMARY KEY (id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================
-- 2. 小组表
-- ============================================================
CREATE TABLE IF NOT EXISTS `groups` (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    pm_id       BIGINT       NOT NULL COMMENT '项目经理ID',
    dev_lead_id BIGINT       NOT NULL COMMENT '组长ID',
    lead_role   VARCHAR(20)  DEFAULT 'dev' COMMENT 'dev=开发组 / test=测试组',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_pm (pm_id),
    INDEX idx_dev_lead (dev_lead_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小组表';

-- ============================================================
-- 3. 项目表
-- ============================================================
CREATE TABLE IF NOT EXISTS projects (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    pm_id        BIGINT       NOT NULL,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    code         VARCHAR(128)          COMMENT '项目编号',
    project_type VARCHAR(50)           COMMENT '项目类型: invite_bidding/ops',
    system_scope TEXT                   COMMENT '系统范围(系统ID列表,逗号分隔)',
    hr_scope     TEXT                   COMMENT '人力资源范围(用户ID列表,逗号分隔)',
    created_by   BIGINT                 COMMENT '创建人ID',
    PRIMARY KEY (id),
    INDEX pm_id (pm_id),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目表';

-- ============================================================
-- 4. 需求表
-- ============================================================
CREATE TABLE IF NOT EXISTS requirements (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    requirement_id         VARCHAR(128) NOT NULL COMMENT '唯一标识 REQ-2026-001',
    number                 VARCHAR(128) NOT NULL COMMENT '需求编号',
    title                  VARCHAR(255)          COMMENT '需求标题',
    description            TEXT                   COMMENT '详细描述',
    status                 VARCHAR(50)  NOT NULL DEFAULT 'planned' COMMENT 'planned/in_progress/integration_test/business_test/pending_release/released/closed',
    priority               VARCHAR(20)  DEFAULT 'medium' COMMENT 'low/medium/high/critical',
    `system`               VARCHAR(50)           COMMENT '所属系统: backend/iOS/Android/鸿蒙/小程序/H5',
    project_id             BIGINT                 COMMENT '关联项目ID',
    project_type           VARCHAR(50)           COMMENT 'ops=运维需求 / project=项目需求',
    person_id              BIGINT                 COMMENT '业务负责人ID',
    total_amount           VARCHAR(128)           COMMENT '总工时/总价',
    dev_total              VARCHAR(128)           COMMENT '开发总工时',
    dev_price              VARCHAR(128)           COMMENT '开发单价',
    test_total             VARCHAR(128)           COMMENT '测试总工时',
    test_price             VARCHAR(128)           COMMENT '测试单价',
    release_time           DATETIME               COMMENT '发布时间',
    iteration_id           VARCHAR(128)           COMMENT '关联发布迭代ID',
    created_at             DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    notes                  TEXT                   COMMENT '备注信息',
    document_path          VARCHAR(500)           COMMENT '文档存储路径',
    document_name          VARCHAR(255)           COMMENT '文档原始文件名',
    document_size          BIGINT                 COMMENT '文档大小(字节)',
    dev_lead_id            BIGINT                 COMMENT '开发组长ID',
    total_price            VARCHAR(128)           COMMENT '总价',
    planned_completion_time DATETIME              COMMENT '计划完成时间',
    person_name            VARCHAR(255)           COMMENT '业务负责人(手输)',
    business_status        VARCHAR(50)            COMMENT '商务状态: pending_bid-待发标, pending_offer-待报价, bidding-待投标, won-已中标',
    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_project (project_id),
    INDEX idx_person (person_id),
    INDEX idx_iteration (iteration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='需求表';

-- ============================================================
-- 5. 发布迭代表
-- ============================================================
CREATE TABLE IF NOT EXISTS iterations (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    iteration_id  VARCHAR(128),
    name          VARCHAR(255) NOT NULL COMMENT '迭代名称',
    release_time  DATETIME              COMMENT '计划发布时间',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    notes         TEXT                   COMMENT '备注',
    created_by    BIGINT                 COMMENT '创建人ID',
    release_notes TEXT                   COMMENT '发布说明及注意事项',
    PRIMARY KEY (id),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发布迭代表';

-- ============================================================
-- 6. 系统信息表
-- ============================================================
CREATE TABLE IF NOT EXISTS systems (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL COMMENT '系统名称',
    it_contact    VARCHAR(255)          COMMENT '甲方IT负责人',
    biz_contact   VARCHAR(255)          COMMENT '甲方业务负责人',
    tech_contact  VARCHAR(255)          COMMENT '内部技术权限负责人',
    created_by    BIGINT                 COMMENT '创建人ID',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    address       TEXT                   COMMENT '系统地址',
    PRIMARY KEY (id),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统管理表';

-- ============================================================
-- 7. 任务表
-- ============================================================
CREATE TABLE IF NOT EXISTS tasks (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
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
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    requirement_id   BIGINT                COMMENT '关联需求ID',
    terminal         VARCHAR(50)           COMMENT '终端',
    progress         INT          DEFAULT 0 COMMENT '进度百分比',
    performance      VARCHAR(128)          COMMENT '绩效数值',
    iteration_id     VARCHAR(128)          COMMENT '关联发布迭代ID',
    test_performance VARCHAR(255)          COMMENT '测试绩效',
    PRIMARY KEY (id),
    INDEX project_id (project_id),
    INDEX creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务表';

-- ============================================================
-- 8. 任务分配人关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS task_assignees (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    task_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    platform   VARCHAR(100),
    status     VARCHAR(50)  DEFAULT 'pending',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX task_id (task_id),
    INDEX user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务分配人表';

-- ============================================================
-- 9. 任务状态历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS task_status_histories (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    task_id     BIGINT       NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50)  NOT NULL,
    changed_by  BIGINT       NOT NULL COMMENT '操作人ID，0=系统自动',
    changed_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    comment     TEXT,
    PRIMARY KEY (id),
    INDEX task_id (task_id),
    INDEX changed_by (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务状态历史表';

-- ============================================================
-- 10. 任务进度上报历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS task_progress_history (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    task_id    BIGINT       NOT NULL,
    progress   INT          NOT NULL COMMENT '0-100',
    comment    VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务进度上报历史';

-- ============================================================
-- 11. Bug 表
-- ============================================================
CREATE TABLE IF NOT EXISTS bugs (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    title          VARCHAR(255) NOT NULL,
    description    TEXT                  COMMENT '实际结果',
    severity       VARCHAR(20)  DEFAULT 'medium',
    status         VARCHAR(50)  NOT NULL DEFAULT 'unfixed',
    task_id        BIGINT       NOT NULL,
    creator_id     BIGINT       NOT NULL,
    assignee_id    BIGINT,
    remark         TEXT                  COMMENT '备注',
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    requirement_id BIGINT                COMMENT '关联需求ID',
    test_type      VARCHAR(20)  DEFAULT 'integration' COMMENT '测试类型: integration-综合测试, business-业务测试',
    expected_result TEXT                   COMMENT '预期结果',
    PRIMARY KEY (id),
    INDEX task_id (task_id),
    INDEX creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Bug表';

-- ============================================================
-- 12. Bug 截图表
-- ============================================================
CREATE TABLE IF NOT EXISTS bug_images (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    bug_id      BIGINT       NOT NULL COMMENT '关联Bug ID',
    image_path  VARCHAR(500) NOT NULL COMMENT '图片存储路径',
    image_name  VARCHAR(255)          COMMENT '原始文件名',
    image_size  BIGINT                COMMENT '文件大小(字节)',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    INDEX idx_bug_id (bug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Bug截图表';

-- ============================================================
-- 13. Bug 状态历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS bug_status_histories (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    bug_id      BIGINT       NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50)  NOT NULL,
    changed_by  BIGINT       NOT NULL COMMENT '操作人ID，0=系统自动',
    changed_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    comment     TEXT,
    PRIMARY KEY (id),
    INDEX bug_id (bug_id),
    INDEX changed_by (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Bug状态历史表';

-- ============================================================
-- 14. 工作流规则表
-- ============================================================
CREATE TABLE IF NOT EXISTS workflow_rules (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    rule_type   VARCHAR(20)  NOT NULL COMMENT 'task / bug / requirement / feature',
    role        VARCHAR(20)  NOT NULL COMMENT 'pm / dev_lead / dev / tester_lead / tester',
    from_status VARCHAR(30)  NOT NULL,
    to_status   VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rule (rule_type, role, from_status, to_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流规则表';

-- ============================================================
-- 15. 基础字典表
-- ============================================================
CREATE TABLE IF NOT EXISTS dictionaries (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    dict_type  VARCHAR(50)  NOT NULL COMMENT '字典类型: system/source/project_type',
    dict_key   VARCHAR(100) NOT NULL COMMENT '字典键',
    dict_value VARCHAR(255) NOT NULL COMMENT '字典值',
    sort_order INT          DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    INDEX idx_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础字典表';

-- ============================================================
-- 16. 站内消息表
-- ============================================================
CREATE TABLE IF NOT EXISTS site_messages (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    type       VARCHAR(20)  NOT NULL DEFAULT 'system',
    related_id BIGINT,
    is_read    TINYINT      NOT NULL DEFAULT 0,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    read_at    DATETIME,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内消息表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 默认管理员 (admin / admin123, 角色 pm)
INSERT IGNORE INTO users (name, password, account, role) VALUES
('admin', '$2a$10$aJF885siaj8Eeg7HHitx9eRsb46zXCbbpQ7Qaq0ngsfqanKXB2Kne', 'admin', 'pm');

-- 工作流规则（任务）
INSERT IGNORE INTO workflow_rules (rule_type, role, from_status, to_status) VALUES
('task', 'pm',         'pending',    'developing'),
('task', 'pm',         'pending',    'testing'),
('task', 'pm',         'pending',    'closed'),
('task', 'pm',         'developing', 'testing'),
('task', 'pm',         'developing', 'closed'),
('task', 'pm',         'testing',    'closed'),
('task', 'pm',         'testing',    'developing'),
('task', 'dev_lead',   'pending',    'developing'),
('task', 'dev_lead',   'developing', 'testing'),
('task', 'dev',        'pending',    'developing'),
('task', 'dev',        'developing', 'testing'),
('task', 'tester',     'pending',    'testing'),
('task', 'tester',     'testing',    'closed'),
('task', 'tester',     'testing',    'developing');

-- 工作流规则（Bug）
INSERT IGNORE INTO workflow_rules (rule_type, role, from_status, to_status) VALUES
('bug', 'tester',   'fixed',     'unfixed'),
('bug', 'tester',   'fixed',     'closed'),
('bug', 'tester',   'not_a_bug', 'unfixed'),
('bug', 'tester',   'not_a_bug', 'closed'),
('bug', 'dev',      'unfixed',   'fixed'),
('bug', 'dev',      'unfixed',   'not_a_bug'),
('bug', 'dev_lead', 'unfixed',   'fixed'),
('bug', 'dev_lead', 'unfixed',   'not_a_bug');

-- 工作流规则（需求）
INSERT IGNORE INTO workflow_rules (rule_type, role, from_status, to_status) VALUES
('requirement', 'pm', 'planned',          'in_progress'),
('requirement', 'pm', 'planned',          'closed'),
('requirement', 'pm', 'in_progress',      'integration_test'),
('requirement', 'pm', 'in_progress',      'closed'),
('requirement', 'pm', 'integration_test', 'business_test'),
('requirement', 'pm', 'integration_test', 'closed'),
('requirement', 'pm', 'business_test',    'pending_release'),
('requirement', 'pm', 'business_test',    'closed'),
('requirement', 'pm', 'pending_release',  'released'),
('requirement', 'pm', 'pending_release',  'business_test');

-- 默认字典数据
INSERT IGNORE INTO dictionaries (dict_type, dict_key, dict_value, sort_order) VALUES
('system',       'backend',  '后台',     '1'),
('system',       'ios',      'iOS',      '2'),
('system',       'android',  '安卓',     '3'),
('system',       'harmony',  '鸿蒙',     '4'),
('system',       'miniapp',  '小程序',   '5'),
('system',       'h5',       'H5',       '6'),
('source',       'internal', '内部需求', '1'),
('source',       'external', '客户需求', '2'),
('project_type', 'ops',      '运维需求', '1'),
('project_type', 'project',  '项目需求', '2'),
('skill',        'backend',  '后台开发', '1'),
('skill',        'ios',      'iOS开发',  '2'),
('skill',        'android',  '安卓开发', '3'),
('skill',        'harmony',  '鸿蒙开发', '4'),
('skill',        'miniapp',  '小程序开发','5'),
('skill',        'h5',       'H5开发',   '6'),
('skill',        'frontend', '前端开发', '7'),
('skill',        'other',    '其他',     '8');
