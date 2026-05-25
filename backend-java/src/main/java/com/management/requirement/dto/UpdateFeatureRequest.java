package com.management.requirement.dto;

import lombok.Data;

@Data
public class UpdateFeatureRequest {
    private String title;
    private String description;
    private Long developerId;
    private Long testerId;
}
