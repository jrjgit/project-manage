package com.management.requirement.dto;

import com.management.bug.entity.Bug;
import com.management.requirement.entity.Requirement;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RequirementDetailDTO {
    private Long id;
    private String requirementId;
    private String number;
    private String title;
    private String description;
    private String notes;
    private String source;
    private String status;
    private String priority;
    private String system;
    private Long projectId;
    private Object project;
    private String projectType;
    private Long personId;
    private Object person;
    private String relevant;
    private String totalAmount;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private String bizTestTotal;
    private String bizTestPrice;
    private String releaseTime;
    private String iterationId;
    private String iterationName;
    private String documentPath;
    private String documentName;
    private Long documentSize;
    private String createdAt;
    private String updatedAt;

    private DevProgress devProgress;
    private List<Bug> integrationTestBugs;
    private List<Bug> businessTestBugs;

    @Data
    public static class DevProgress {
        private List<TerminalProgress> terminals;
    }

    @Data
    public static class TerminalProgress {
        private String terminal;
        private int progress;
        private long total;
        private long done;
        private long overdueDays;
        private List<TaskBrief> tasks;
    }

    @Data
    public static class TaskBrief {
        private Long id;
        private String title;
        private String status;
    }
}
