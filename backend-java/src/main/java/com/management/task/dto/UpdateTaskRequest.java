package com.management.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "更新任务请求")
@Data
public class UpdateTaskRequest {
    /** 任务标题 */
    private String title;
    /** 任务描述 */
    private String description;
    /** 优先级 */
    private String priority;
    /** 项目ID */
    private Long projectId;
    /** 开发组长ID */
    private Long devLeadId;
    /** 指派人ID */
    private Long assigneeId;
    /** 测试人员ID */
    private Long testerId;
    /** 截止日期 */
    private String deadline;
    /** 绩效工时 */
    private String performance;
    /** 关联需求ID */
    private Long requirementId;
    /** 技能要求 */
    private String terminal;
    /** 开发进度 */
    private Integer progress;
    /** 关联迭代ID */
    private String iterationId;
    /** 测试绩效工时 */
    private String testPerformance;
}
