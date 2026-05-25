package com.management.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeTaskStatusRequest {
    @NotBlank
    private String newStatus;
    private String comment;
}
