package com.management.bug.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBugRequest {
    @NotBlank private String title;
    private String description;
    private String severity;
    private Long taskId;
    private Long assigneeId;
    private String testType;
    private Long requirementId;
}
