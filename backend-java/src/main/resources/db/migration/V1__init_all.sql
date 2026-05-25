-- ============================================
-- 管理系统数据库初始化脚本
-- 适用于 MySQL 8.x
-- 首次部署时执行此文件即可创建全部表结构
-- ============================================

CREATE DATABASE IF NOT EXISTS management
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE management;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL           COMMENT 'pm / dev_lead / dev / tester_lead / tester',
    group_id    BIGINT                          COMMENT '所属组ID',
    wechat_id   VARCHAR(255)                    COMMENT '企业微信ID',
    dingtalk_id VARCHAR(255)                    COMMENT '钉钉ID',
    feishu_id   VARCHAR(255)                    COMMENT '飞书ID',
    email       VARCHAR(255)                    COMMENT '邮箱',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 项目表
-- ----------------------------
CREATE TABLE IF NOT EXISTS projects (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    pm_id      BIGINT       NOT NULL            COMMENT '项目经理ID',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pm (pm_id),
    FOREIGN KEY (pm_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ----------------------------
-- 3. 任务表
-- ----------------------------
CREATE TABLE IF NOT EXISTS tasks (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'pending'
                        COMMENT 'pending/assigned_lead/developing/developed/pending_test/testing/passed/rejected/closed',
    priority        VARCHAR(20)  DEFAULT 'medium'
                        COMMENT 'low/medium/high/critical',
    project_id      BIGINT,
    creator_id      BIGINT       NOT NULL       COMMENT '创建人ID',
    assignee_id     BIGINT                       COMMENT '主开发人员ID',
    dev_lead_id     BIGINT                       COMMENT '开发组长ID',
    tester_lead_id  BIGINT                       COMMENT '测试组长ID',
    tester_id       BIGINT                       COMMENT '测试人员ID',
    reject_reason   TEXT                         COMMENT '打回原因',
    deadline        DATETIME                     COMMENT '截止日期',
    -- 新增字段 (S1)
    requirement_id  BIGINT                       COMMENT '关联需求ID',
    feature_id      BIGINT                       COMMENT '关联功能点ID',
    terminal        VARCHAR(50)                  COMMENT '终端: backend/iOS/Android/鸿蒙/小程序/H5',
    progress        INT          DEFAULT 0       COMMENT '进度百分比 0-100',
    performance     VARCHAR(128)                 COMMENT '绩效数值',
    created_at      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_project (project_id),
    INDEX idx_creator (creator_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_dev_lead (dev_lead_id),
    INDEX idx_requirement (requirement_id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- ----------------------------
-- 4. 任务分配人关联表（多开发人员）
-- ----------------------------
CREATE TABLE IF NOT EXISTS task_assignees (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    platform   VARCHAR(100)                     COMMENT '平台标识',
    status     VARCHAR(50)  DEFAULT 'pending'   COMMENT 'pending/developing/developed',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task (task_id),
    INDEX idx_user (user_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分配人表';

-- ----------------------------
-- 5. 任务状态历史表
-- ----------------------------
CREATE TABLE IF NOT EXISTS task_status_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id     BIGINT       NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50)  NOT NULL,
    changed_by  BIGINT       NOT NULL           COMMENT '操作人ID，0=系统自动',
    changed_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    comment     TEXT,
    INDEX idx_task (task_id),
    INDEX idx_changed_by (changed_by),
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (changed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务状态历史表';

-- ----------------------------
-- 6. Bug表
-- ----------------------------
CREATE TABLE IF NOT EXISTS bugs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    severity      VARCHAR(20)  DEFAULT 'medium'
                        COMMENT 'low/medium/high/critical',
    status        VARCHAR(50)  NOT NULL DEFAULT 'new'
                        COMMENT 'new/assigned/fixing/fixed/pending_verify/closed/reopened',
    task_id       BIGINT       NOT NULL         COMMENT '关联任务ID',
    creator_id    BIGINT       NOT NULL         COMMENT '创建人ID(测试人员)',
    assignee_id   BIGINT                        COMMENT '指派修复人ID',
    fix_comment   TEXT                          COMMENT '修复说明',
    reopen_reason TEXT                          COMMENT '重新打开原因',
    -- 新增字段 (S1)
    requirement_id BIGINT                       COMMENT '关联需求ID',
    developer_id   BIGINT                       COMMENT '开发负责人ID',
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_task (task_id),
    INDEX idx_creator (creator_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_requirement (requirement_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bug表';

-- ----------------------------
-- 7. Bug状态历史表
-- ----------------------------
CREATE TABLE IF NOT EXISTS bug_status_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    bug_id      BIGINT       NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50)  NOT NULL,
    changed_by  BIGINT       NOT NULL           COMMENT '操作人ID，0=系统自动',
    changed_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    comment     TEXT,
    INDEX idx_bug (bug_id),
    INDEX idx_changed_by (changed_by),
    FOREIGN KEY (bug_id) REFERENCES bugs(id),
    FOREIGN KEY (changed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bug状态历史表';

-- ----------------------------
-- 8. 小组表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `groups` (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    pm_id       BIGINT       NOT NULL           COMMENT '项目经理ID',
    dev_lead_id BIGINT       NOT NULL           COMMENT '组长ID',
    lead_role   VARCHAR(20)  DEFAULT 'dev'      COMMENT 'dev=开发组 / test=测试组',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pm (pm_id),
    INDEX idx_dev_lead (dev_lead_id),
    FOREIGN KEY (pm_id) REFERENCES users(id),
    FOREIGN KEY (dev_lead_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小组表';

-- ----------------------------
-- 9. 工作流规则表
-- ----------------------------
CREATE TABLE IF NOT EXISTS workflow_rules (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type   VARCHAR(20)  NOT NULL           COMMENT 'task / bug / requirement / feature',
    role        VARCHAR(20)  NOT NULL           COMMENT 'pm / dev_lead / dev / tester_lead / tester',
    from_status VARCHAR(30)  NOT NULL,
    to_status   VARCHAR(30)  NOT NULL,
    UNIQUE KEY uk_rule (rule_type, role, from_status, to_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流规则表';

-- ----------------------------
-- 10. 需求表 (新增 S1)
-- ----------------------------
CREATE TABLE IF NOT EXISTS requirements (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id   VARCHAR(128) NOT NULL      COMMENT '唯一标识 REQ-2026-001',
    number           VARCHAR(128) NOT NULL      COMMENT '需求编号',
    title            VARCHAR(255) NOT NULL      COMMENT '需求标题',
    description      TEXT                       COMMENT '详细描述',
    source           VARCHAR(50)                COMMENT '需求来源: internal/external',
    status           VARCHAR(50)  NOT NULL DEFAULT 'planned'
                        COMMENT 'planned/in_progress/integration_test/business_test/pending_release/released/closed',
    priority         VARCHAR(20)  DEFAULT 'medium'
                        COMMENT 'low/medium/high/critical',
    `system`         VARCHAR(50)                COMMENT '所属系统: backend/iOS/Android/鸿蒙/小程序/H5',
    project_id       BIGINT                     COMMENT '关联项目ID',
    project_type     VARCHAR(50)                COMMENT 'ops=运维需求 / project=项目需求',
    person_id        BIGINT                     COMMENT '业务负责人ID',
    relevant         TEXT                       COMMENT '相关人员',
    total_amount     VARCHAR(128)               COMMENT '总工时/总价',
    dev_total        VARCHAR(128)               COMMENT '开发总工时',
    dev_price        VARCHAR(128)               COMMENT '开发单价',
    test_total       VARCHAR(128)               COMMENT '测试总工时',
    test_price       VARCHAR(128)               COMMENT '测试单价',
    biz_test_total   VARCHAR(128)               COMMENT '业务测试总工时',
    biz_test_price   VARCHAR(128)               COMMENT '业务测试单价',
    release_time     DATETIME                   COMMENT '发布时间',
    iteration_id     VARCHAR(128)               COMMENT '关联发布迭代ID',
    created_at       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_project (project_id),
    INDEX idx_person (person_id),
    INDEX idx_iteration (iteration_id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (person_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

-- ----------------------------
-- 11. 功能点表 (新增 S1)
-- ----------------------------
CREATE TABLE IF NOT EXISTS features (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT       NOT NULL        COMMENT '关联需求ID',
    title          VARCHAR(255) NOT NULL        COMMENT '功能点标题',
    description    TEXT                         COMMENT '功能点描述',
    status         VARCHAR(50)  NOT NULL DEFAULT 'planned'
                        COMMENT 'planned/developing/pending_test/closed',
    developer_id   BIGINT                       COMMENT '开发负责人ID',
    tester_id      BIGINT                       COMMENT '测试负责人ID',
    created_at     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_requirement (requirement_id),
    INDEX idx_developer (developer_id),
    FOREIGN KEY (requirement_id) REFERENCES requirements(id),
    FOREIGN KEY (developer_id) REFERENCES users(id),
    FOREIGN KEY (tester_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能点表';

-- ----------------------------
-- 12. 发布迭代表 (新增 S1)
-- ----------------------------
CREATE TABLE IF NOT EXISTS iterations (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    iteration_id  VARCHAR(128) NOT NULL         COMMENT '唯一标识 ITER-2026-001',
    name          VARCHAR(255) NOT NULL         COMMENT '迭代名称',
    release_time  DATETIME                      COMMENT '计划发布时间',
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布迭代表';

-- ----------------------------
-- 13. 基础字典表 (新增 S1)
-- ----------------------------
CREATE TABLE IF NOT EXISTS dictionaries (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type  VARCHAR(50)  NOT NULL            COMMENT '字典类型: system/source/project_type',
    dict_key   VARCHAR(100) NOT NULL            COMMENT '字典键',
    dict_value VARCHAR(255) NOT NULL            COMMENT '字典值',
    sort_order INT          DEFAULT 0           COMMENT '排序',
    INDEX idx_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础字典表';
