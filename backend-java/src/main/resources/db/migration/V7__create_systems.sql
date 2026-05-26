CREATE TABLE IF NOT EXISTS systems (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL             COMMENT '系统名称',
    it_contact    VARCHAR(255)                      COMMENT '甲方IT负责人',
    biz_contact   VARCHAR(255)                      COMMENT '甲方业务负责人',
    tech_contact  VARCHAR(255)                      COMMENT '内部技术权限负责人',
    created_by    BIGINT                            COMMENT '创建人ID',
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理表';
