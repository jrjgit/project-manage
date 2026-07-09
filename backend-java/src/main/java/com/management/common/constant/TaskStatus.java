package com.management.common.constant;

/**
 * 任务状态常量
 */
public final class TaskStatus {

    private TaskStatus() {}

    /** 待受理 */
    public static final String PENDING = "pending";
    /** 开发中 */
    public static final String DEVELOPING = "developing";
    /** 已开发完成（中间状态，自动跳转为待测试） */
    public static final String DEVELOPED = "developed";
    /** 待测试 */
    public static final String PENDING_TEST = "pending_test";
    /** 综合测试中 */
    public static final String TESTING = "testing";
    /** 已完成 */
    public static final String CLOSED = "closed";
    /** 已驳回 */
    public static final String REJECTED = "rejected";
}
