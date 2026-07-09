package com.management.common.constant;

/**
 * Bug 状态常量
 */
public final class BugStatus {

    private BugStatus() {}

    /** 未修复 */
    public static final String UNFIXED = "unfixed";
    /** 已修复 */
    public static final String FIXED = "fixed";
    /** 待验证 */
    public static final String PENDING_VERIFY = "pending_verify";
    /** 已关闭 */
    public static final String CLOSED = "closed";
}
