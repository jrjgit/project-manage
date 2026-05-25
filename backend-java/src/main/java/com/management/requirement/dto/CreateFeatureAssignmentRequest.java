package com.management.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateFeatureAssignmentRequest {
    private Long featureId;
    @NotBlank
    private String terminal;
    @NotNull
    private Long developerId;
}
