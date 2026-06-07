package com.management.auth;

import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "登录响应")
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private User user;
}
