CREATE TABLE task_progress_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    progress INT NOT NULL COMMENT '0-100',
    comment VARCHAR(500) DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务进度上报历史';
