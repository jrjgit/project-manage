package com.management.requirement.dto;

import lombok.Data;

@Data
public class UpdateRequirementRequest {
    private String description;
    private String notes;
    private String system;
    private Long projectId;
    private Long personId;
    private String personName;
    private String priority;
    private String status;
    private String totalAmount;
    private String totalPrice;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private String requirementId;
    private String number;
    private String iterationId;
    private String projectType;
    private String plannedCompletionTime;
}
