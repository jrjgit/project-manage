package com.management.requirement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeatureDetailDTO {
    private Long id;
    private Long requirementId;
    private String title;
    private String description;
    private String status;
    private Long developerId;
    private Object developer;
    private Long testerId;
    private Object tester;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TaskBrief> tasks;

    @Data
    public static class TaskBrief {
        private Long id;
        private String title;
        private String status;
        private String terminal;
        private Integer progress;
    }
}
