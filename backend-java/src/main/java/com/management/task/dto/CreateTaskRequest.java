package com.management.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateTaskRequest {
    @NotBlank
    private String title;
    private String description;
    private String priority;
    @NotNull
    private Long projectId;
    private Long devLeadId;
    private Long assigneeId;
    private List<Long> assigneeIds;
    private List<TaskAssigneeItem> assignees;
    private Long testerId;
    private String deadline;
    private String performance;
    private Long requirementId;
    private Long featureId;
    private String terminal;
    private String iterationId;

    @Data
    public static class TaskAssigneeItem {
        @NotNull
        private Long userId;
        private String platform;
    }
}
