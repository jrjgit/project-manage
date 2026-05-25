package com.management.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTaskAssigneeRequest {
    @NotNull
    private Long userId;
    private String platform;
}
