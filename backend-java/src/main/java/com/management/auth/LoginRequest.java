package com.management.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "登录请求")
@Data
public class LoginRequest {
    /** 登录账号 */
    @NotBlank
    private String account;
    /** 登录密码 */
    @NotBlank
    private String password;
}
