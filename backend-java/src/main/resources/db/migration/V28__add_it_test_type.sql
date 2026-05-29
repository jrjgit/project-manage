ALTER TABLE bugs MODIFY test_type VARCHAR(20) DEFAULT 'integration'
    COMMENT '测试类型: integration-综合测试, business-业务测试, it_test-IT测试';
