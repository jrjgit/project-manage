ALTER TABLE projects
    ADD COLUMN code          VARCHAR(128)          COMMENT '项目编号',
    ADD COLUMN project_type  VARCHAR(50)           COMMENT '项目类型: invite_bidding/ops',
    ADD COLUMN system_scope  TEXT                  COMMENT '系统范围(系统ID列表,逗号分隔)',
    ADD COLUMN hr_scope      TEXT                  COMMENT '人力资源范围(用户ID列表,逗号分隔)',
    ADD COLUMN created_by    BIGINT                COMMENT '创建人ID',
    ADD INDEX idx_created_by (created_by),
    ADD FOREIGN KEY (created_by) REFERENCES users(id);
