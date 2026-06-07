package com.management.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "变更Bug状态请求")
@Data
public class ChangeBugStatusRequest {
    /** 目标状态 */
    @NotBlank private String newStatus;
    /** 变更备注 */
    private String comment;
}
