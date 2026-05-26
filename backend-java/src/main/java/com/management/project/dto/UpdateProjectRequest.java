package com.management.project.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateProjectRequest {
    private String name;
    private String code;
    private String projectType;
    private List<Long> systemScope;
    private List<Long> hrScope;
    private Long pmId;
}
