package com.management.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "添加任务指派人请求")
@Data
public class AddTaskAssigneeRequest {
    /** 用户ID */
    @NotNull
    private Long userId;
    /** 平台信息 */
    private String platform;
}
