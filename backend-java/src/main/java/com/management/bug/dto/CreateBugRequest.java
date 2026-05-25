package com.management.bug.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBugRequest {
    @NotBlank private String title;
    private String description;
    private String severity;
    @NotNull private Long taskId;
    @NotNull private Long assigneeId;
}
