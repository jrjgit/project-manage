package com.management.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateProjectRequest {
    @NotBlank
    private String name;
    private String code;
    private String projectType;
    private List<Long> systemScope;
    private List<Long> hrScope;
}
