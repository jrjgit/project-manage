package com.management.bug.dto;

import lombok.Data;

@Data
public class UpdateBugRequest {
    private String title;
    private String description;
    private String severity;
    private Long taskId;
    private Long assigneeId;
    private String fixComment;
    private String reopenReason;
    private String testType;
}
