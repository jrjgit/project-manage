package com.management.requirement.dto;

import lombok.Data;

@Data
public class UpdateRequirementRequest {
    private String title;
    private String description;
    private String notes;
    private String source;
    private String system;
    private Long projectId;
    private Long personId;
    private String relevant;
    private String priority;
    private String totalAmount;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private String bizTestTotal;
    private String bizTestPrice;
    private String requirementId;
    private String iterationId;
}
