ALTER TABLE iterations
    ADD COLUMN notes       TEXT                  COMMENT '备注',
    ADD COLUMN created_by  BIGINT                COMMENT '创建人ID',
    ADD INDEX idx_created_by (created_by),
    ADD FOREIGN KEY (created_by) REFERENCES users(id);
