package com.management.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRequirementRequest {
    @NotBlank private String title;
    private String description;
    private String notes;
    private String source;
    private String system;
    private Long projectId;
    private String projectType;
    private Long personId;
    private String personName;
    private String relevant;
    private String priority;
    private String totalAmount;
    private String totalPrice;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private String bizTestTotal;
    private String bizTestPrice;
    private String iterationId;
    private String number;
    private String plannedCompletionTime;
}
