package com.management.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateFeatureRequest {
    @NotBlank private String title;
    private String description;
    @NotNull private Long developerId;
    private Long testerId;
}
