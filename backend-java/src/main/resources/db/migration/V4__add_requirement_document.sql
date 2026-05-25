-- 需求表添加文档字段和备注字段
ALTER TABLE requirements
    ADD COLUMN notes TEXT COMMENT '备注信息',
    ADD COLUMN document_path VARCHAR(500) COMMENT '文档存储路径',
    ADD COLUMN document_name VARCHAR(255) COMMENT '文档原始文件名',
    ADD COLUMN document_size BIGINT COMMENT '文档大小(字节)';
