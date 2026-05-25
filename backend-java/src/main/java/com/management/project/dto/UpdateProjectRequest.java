package com.management.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @NotBlank
    private String name;
}
