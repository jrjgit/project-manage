package com.management.group.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotNull
    private Long userId;
}
