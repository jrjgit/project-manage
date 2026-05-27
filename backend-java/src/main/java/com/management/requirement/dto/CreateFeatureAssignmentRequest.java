package com.management.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFeatureAssignmentRequest {
    private Long featureId;
    @NotBlank
    private String terminal;
    private Long developerId;
    private String description;
    private String performance;
    private String deadline;
    private String notes;
}
