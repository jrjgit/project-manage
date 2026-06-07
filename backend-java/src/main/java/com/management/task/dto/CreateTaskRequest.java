package com.management.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Schema(description = "创建任务请求")
@Data
public class CreateTaskRequest {
    /** 任务标题 */
    @NotBlank
    private String title;
    /** 任务描述 */
    private String description;
    /** 优先级 */
    private String priority;
    /** 项目ID */
    @NotNull
    private Long projectId;
    /** 开发组长ID */
    private Long devLeadId;
    /** 指派人ID */
    private Long assigneeId;
    /** 指派人ID列表 */
    private List<Long> assigneeIds;
    /** 指派人列表 */
    private List<TaskAssigneeItem> assignees;
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
    /** 关联迭代ID */
    private String iterationId;
    /** 测试绩效工时 */
    private String testPerformance;

    @Data
    public static class TaskAssigneeItem {
        /** 用户ID */
        @NotNull
        private Long userId;
        /** 平台信息 */
        private String platform;
    }
}
