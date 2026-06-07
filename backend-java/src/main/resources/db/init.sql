-- =============================================
-- 管理系统数据库完整初始化脚本
-- 适用于 MySQL 8.x
-- 基于 V1~V36 迁移脚本最终状态生成
-- 全新部署时执行此文件即可创建完整表结构及初始数据
-- =============================================

CREATE DATABASE IF NOT EXISTS management
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE management;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    name        VARCHAR(255) NOT NULL COMMENT '用户姓名',
    account     VARCHAR(255) COMMENT '登录账号',
    password    VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    role        VARCHAR(50)  NOT NULL COMMENT '角色: pm-项目经理, dev_lead-开发组长, dev-开发, tester-测试',
    wechat_id   VARCHAR(255) COMMENT '企业微信ID',
    dingtalk_id VARCHAR(255) COMMENT '钉钉ID',
    feishu_id   VARCHAR(255) COMMENT '飞书ID',
    email       VARCHAR(255) COMMENT '邮箱',
    skills      VARCHAR(500) COMMENT '技能列表(逗号分隔): iOS,Android,鸿蒙,小程序,H5,后台,前端,其他',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 项目表
-- ============================================================
CREATE TABLE IF NOT EXISTS projects (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    name         VARCHAR(255) NOT NULL COMMENT '项目名称',
    code         VARCHAR(128) COMMENT '项目编号',
    project_type VARCHAR(50)  COMMENT '项目类型: invite_bidding-邀请招标, ops-运维项目',
    system_scope TEXT         COMMENT '系统范围(系统ID列表,逗号分隔)',
    hr_scope     TEXT         COMMENT '人力资源范围(用户ID列表,逗号分隔)',
    pm_id        BIGINT       COMMENT '项目经理ID',
    created_by   BIGINT       COMMENT '创建人ID',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_pm (pm_id),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ============================================================
-- 3. 任务表
-- ============================================================
CREATE TABLE IF NOT EXISTS tasks (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    title            VARCHAR(255) NOT NULL COMMENT '任务标题',
    description      TEXT         COMMENT '任务详细描述',
    status           VARCHAR(50)  NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待分配, developing-开发中, testing-测试中, closed-已关闭',
    priority         VARCHAR(20)  DEFAULT 'medium' COMMENT '优先级: low-低, medium-中, high-高, critical-紧急',
    project_id       BIGINT       COMMENT '关联项目ID',
    creator_id       BIGINT       NOT NULL COMMENT '创建人ID',
    assignee_id      BIGINT       COMMENT '主开发人员ID',
    dev_lead_id      BIGINT       COMMENT '开发组长ID',
    tester_id        BIGINT       COMMENT '测试人员ID',
    reject_reason    TEXT         COMMENT '打回原因',
    deadline         DATETIME     COMMENT '截止日期',
    requirement_id   BIGINT       COMMENT '关联需求ID',
    terminal         VARCHAR(50)  COMMENT '终端: backend-后台, iOS, Android, 鸿蒙, 小程序, H5',
    progress         INT          DEFAULT 0 COMMENT '开发进度百分比(0-100)',
    performance      VARCHAR(128) COMMENT '开发绩效数值',
    test_performance VARCHAR(255) COMMENT '测试绩效数值',
    iteration_id     VARCHAR(128) COMMENT '关联发布迭代ID',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_project (project_id),
    INDEX idx_creator (creator_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_dev_lead (dev_lead_id),
    INDEX idx_requirement (requirement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- ============================================================
-- 4. 任务分配人关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS task_assignees (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    task_id    BIGINT       NOT NULL COMMENT '任务ID',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    platform   VARCHAR(100) COMMENT '平台标识',
    status     VARCHAR(50)  DEFAULT 'pending' COMMENT '状态: pending-待开发, developing-开发中, developed-已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task (task_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分配人表';

-- ============================================================
-- 5. 任务状态历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS task_status_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    task_id     BIGINT       NOT NULL COMMENT '任务ID',
    from_status VARCHAR(50)  COMMENT '变更前状态',
    to_status   VARCHAR(50)  NOT NULL COMMENT '变更后状态',
    changed_by  BIGINT       NOT NULL COMMENT '操作人ID(0=系统自动)',
    changed_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    comment     TEXT         COMMENT '备注说明',
    INDEX idx_task (task_id),
    INDEX idx_changed_by (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务状态历史表';

-- ============================================================
-- 6. 任务进度上报历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS task_progress_history (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    task_id    BIGINT       NOT NULL COMMENT '任务ID',
    progress   INT          NOT NULL COMMENT '进度百分比(0-100)',
    comment    VARCHAR(500) COMMENT '进度说明',
    created_by BIGINT       COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务进度上报历史表';

-- ============================================================
-- 7. Bug表
-- ============================================================
CREATE TABLE IF NOT EXISTS bugs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Bug ID',
    title          VARCHAR(255) NOT NULL COMMENT 'Bug标题',
    description    TEXT         COMMENT 'Bug详细描述',
    severity       VARCHAR(20)  DEFAULT 'medium' COMMENT '严重程度: low-低, medium-中, high-高, critical-紧急',
    status         VARCHAR(50)  NOT NULL DEFAULT 'unfixed' COMMENT '状态: unfixed-未修复, fixed-已修复, not_a_bug-非Bug, closed-已关闭',
    task_id        BIGINT       NOT NULL COMMENT '关联任务ID',
    creator_id     BIGINT       NOT NULL COMMENT '创建人ID',
    assignee_id    BIGINT       COMMENT '指派修复人ID',
    remark         TEXT         COMMENT '备注说明',
    image_path     VARCHAR(500) COMMENT 'Bug图片存储路径',
    image_name     VARCHAR(255) COMMENT 'Bug图片原始文件名',
    image_size     BIGINT       COMMENT 'Bug图片大小(字节)',
    requirement_id BIGINT       COMMENT '关联需求ID',
    test_type      VARCHAR(20)  DEFAULT 'integration' COMMENT '测试类型: integration-综合测试, business-业务测试, it_test-IT测试',
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_task (task_id),
    INDEX idx_creator (creator_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_requirement (requirement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bug表';

-- ============================================================
-- 8. Bug状态历史表
-- ============================================================
CREATE TABLE IF NOT EXISTS bug_status_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    bug_id      BIGINT       NOT NULL COMMENT 'Bug ID',
    from_status VARCHAR(50)  COMMENT '变更前状态',
    to_status   VARCHAR(50)  NOT NULL COMMENT '变更后状态',
    changed_by  BIGINT       NOT NULL COMMENT '操作人ID(0=系统自动)',
    changed_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    comment     TEXT         COMMENT '备注说明',
    INDEX idx_bug (bug_id),
    INDEX idx_changed_by (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bug状态历史表';

-- ============================================================
-- 9. 工作流规则表
-- ============================================================
CREATE TABLE IF NOT EXISTS workflow_rules (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_type   VARCHAR(20)  NOT NULL COMMENT '规则类型: task-任务, bug-Bug, requirement-需求',
    role        VARCHAR(20)  NOT NULL COMMENT '角色: pm, dev_lead, dev, tester',
    from_status VARCHAR(30)  NOT NULL COMMENT '源状态',
    to_status   VARCHAR(30)  NOT NULL COMMENT '目标状态',
    UNIQUE KEY uk_rule (rule_type, role, from_status, to_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流规则表';

-- ============================================================
-- 10. 需求表
-- ============================================================
CREATE TABLE IF NOT EXISTS requirements (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '需求ID',
    requirement_id         VARCHAR(128) NOT NULL COMMENT '需求唯一标识(如REQ-2026-001)',
    number                 VARCHAR(128) NOT NULL COMMENT '需求编号',
    title                  VARCHAR(255) COMMENT '需求标题',
    description            TEXT         COMMENT '需求详细描述',
    notes                  TEXT         COMMENT '备注信息',
    status                 VARCHAR(50)  NOT NULL DEFAULT 'planned' COMMENT '状态: planned-已规划, in_progress-进行中, integration_test-综合测试, business_test-业务测试, pending_release-待发布, released-已发布, closed-已关闭',
    priority               VARCHAR(20)  DEFAULT 'medium' COMMENT '优先级: low-低, medium-中, high-高, critical-紧急',
    `system`               VARCHAR(50)  COMMENT '所属系统: backend-后台, iOS, Android, 鸿蒙, 小程序, H5',
    project_id             BIGINT       COMMENT '关联项目ID',
    project_type           VARCHAR(50)  COMMENT '项目类型: ops-运维需求, project-项目需求',
    person_id              BIGINT       COMMENT '业务负责人ID',
    person_name            VARCHAR(255) COMMENT '业务负责人姓名(手输)',
    total_amount           VARCHAR(128) COMMENT '总工时',
    total_price            VARCHAR(128) COMMENT '总价',
    dev_total              VARCHAR(128) COMMENT '开发总工时',
    dev_price              VARCHAR(128) COMMENT '开发单价',
    test_total             VARCHAR(128) COMMENT '测试总工时',
    test_price             VARCHAR(128) COMMENT '测试单价',
    release_time           DATETIME     COMMENT '发布时间',
    planned_completion_time DATETIME    COMMENT '计划完成时间',
    dev_lead_id            BIGINT       COMMENT '开发组长ID',
    business_status        VARCHAR(50)  COMMENT '商务状态: pending_bid-待发标, pending_offer-待报价, bidding-待投标, won-已中标',
    iteration_id           VARCHAR(128) COMMENT '关联发布迭代ID',
    document_path          VARCHAR(500) COMMENT '文档存储路径',
    document_name          VARCHAR(255) COMMENT '文档原始文件名',
    document_size          BIGINT       COMMENT '文档大小(字节)',
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at             DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_project (project_id),
    INDEX idx_person (person_id),
    INDEX idx_iteration (iteration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

-- ============================================================
-- 11. 发布迭代表
-- ============================================================
CREATE TABLE IF NOT EXISTS iterations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '迭代ID',
    name         VARCHAR(255) NOT NULL COMMENT '迭代名称',
    release_time DATETIME     COMMENT '计划发布时间',
    notes        TEXT         COMMENT '备注',
    release_notes TEXT        COMMENT '发布说明及注意事项',
    created_by   BIGINT       COMMENT '创建人ID',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布迭代表';

-- ============================================================
-- 12. 系统信息表
-- ============================================================
CREATE TABLE IF NOT EXISTS systems (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '系统ID',
    name         VARCHAR(255) NOT NULL COMMENT '系统名称',
    it_contact   VARCHAR(255) COMMENT '甲方IT负责人',
    biz_contact  VARCHAR(255) COMMENT '甲方业务负责人',
    tech_contact VARCHAR(255) COMMENT '内部技术权限负责人',
    address      TEXT         COMMENT '系统地址',
    created_by   BIGINT       COMMENT '创建人ID',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统信息表';

-- ============================================================
-- 13. 基础字典表
-- ============================================================
CREATE TABLE IF NOT EXISTS dictionaries (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_type  VARCHAR(50)  NOT NULL COMMENT '字典类型: system-系统, source-来源, project_type-项目类型, skill-技能',
    dict_key   VARCHAR(100) NOT NULL COMMENT '字典键',
    dict_value VARCHAR(255) NOT NULL COMMENT '字典值',
    sort_order INT          DEFAULT 0 COMMENT '排序号',
    INDEX idx_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础字典表';

-- ============================================================
-- 14. 站内消息表
-- ============================================================
CREATE TABLE IF NOT EXISTS site_messages (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    user_id    BIGINT       NOT NULL COMMENT '接收用户ID',
    title      VARCHAR(255) NOT NULL COMMENT '消息标题',
    content    TEXT         NOT NULL COMMENT '消息内容',
    type       VARCHAR(20)  NOT NULL DEFAULT 'system' COMMENT '消息类型: system-系统, task-任务, bug-Bug, requirement-需求',
    related_id BIGINT       COMMENT '关联业务ID',
    is_read    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    read_at    DATETIME     COMMENT '阅读时间',
    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 默认管理员 (admin / admin123, 角色 pm)
INSERT IGNORE INTO users (name, account, password, role) VALUES
('admin', 'admin', '$2b$10$Q/XAfu9Qocng8IUfdcMpJOmGUpy.WgAkNutehRF8KbvQSAaqQUE7q', 'pm');

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
