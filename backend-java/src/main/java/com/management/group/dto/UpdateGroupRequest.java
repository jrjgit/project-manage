package com.management.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGroupRequest {
    @NotBlank
    private String name;
    private Long devLeadId;
    private String leadRole;
}
