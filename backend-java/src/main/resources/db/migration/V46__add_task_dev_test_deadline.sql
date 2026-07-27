-- 为任务表新增测试截止时间
-- 优化需求5:
--   1) 任务派发弹窗里"计划完成时间"改名为"开发截止时间"（复用原 deadline 字段，仅改标签，不新增列）
--   2) 新增"测试截止时间"字段
--   3) 任务摘要"截止日期"展示为"需求截止日期"，新增"测试截止日期"
ALTER TABLE tasks
    ADD COLUMN test_deadline DATETIME NULL COMMENT '测试截止时间' AFTER deadline;