package com.management.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "批量指派任务测试人员请求")
@Data
public class AddTaskTestersRequest {
    /** 测试人员用户ID列表 */
    @NotEmpty
    private List<Long> userIds;
}