package com.management.common.constant;

/**
 * 需求状态常量
 */
public final class RequirementStatus {

    private RequirementStatus() {}

    /** 待任务分配 */
    public static final String PENDING_TASK = "pending_task";
    /** 进行中 */
    public static final String IN_PROGRESS = "in_progress";
    /** 综合测试 */
    public static final String INTEGRATION_TEST = "integration_test";
    /** 业务测试 */
    public static final String BUSINESS_TEST = "business_test";
    /** 待发布 */
    public static final String PENDING_RELEASE = "pending_release";
    /** 已发布 */
    public static final String RELEASED = "released";
    /** 已关闭 */
    public static final String CLOSED = "closed";
}
