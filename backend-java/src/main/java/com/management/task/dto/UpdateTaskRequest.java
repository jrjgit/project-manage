package com.management.task.dto;

import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private String priority;
    private Long projectId;
    private Long devLeadId;
    private Long assigneeId;
    private Long testerLeadId;
    private Long testerId;
    private String deadline;
    private String performance;
    private Long requirementId;
    private Long featureId;
    private String terminal;
    private Integer progress;
    private String iterationId;
    private String testPerformance;
}
