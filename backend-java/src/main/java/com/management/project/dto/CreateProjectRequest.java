package com.management.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank
    private String name;
}
