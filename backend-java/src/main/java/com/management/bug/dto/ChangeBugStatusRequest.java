package com.management.bug.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeBugStatusRequest {
    @NotBlank private String newStatus;
    private String comment; // 备注
}
