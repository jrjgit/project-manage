ALTER TABLE requirements
    ADD COLUMN total_price           VARCHAR(128)          COMMENT '总价',
    ADD COLUMN planned_completion_time DATETIME             COMMENT '计划完成时间';
