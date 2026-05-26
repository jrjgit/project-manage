package com.management.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String account;
    @NotBlank @Size(min = 6)
    private String password;
    @NotBlank
    private String role;
    private Long groupId;
    private String wechatId;
    private String email;
}
