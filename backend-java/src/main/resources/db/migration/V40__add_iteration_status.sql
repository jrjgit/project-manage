ALTER TABLE iterations ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'pending_publish' COMMENT '迭代状态：pending_publish=待发布, published=已发布';
