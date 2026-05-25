SET @dbname = DATABASE();
SET @exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'bugs' AND COLUMN_NAME = 'test_type');
SET @sql = IF(@exists = 0,
  'ALTER TABLE bugs ADD COLUMN test_type VARCHAR(20) DEFAULT ''integration'' COMMENT ''测试类型: integration-综合测试, business-业务测试''',
  'SELECT ''column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
