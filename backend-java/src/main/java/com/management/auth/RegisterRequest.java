package com.management.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "注册请求")
@Data
public class RegisterRequest {
    /** 姓名 */
    @NotBlank
    private String name;
    /** 登录账号 */
    @NotBlank
    private String account;
    /** 登录密码 */
    @NotBlank @Size(min = 6)
    private String password;
    /** 角色 */
    @NotBlank
    private String role;
    /** 邮箱 */
    private String email;
}
