package com.management.auth;

import com.management.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        Long userId = authService.register(req);
        return Result.ok(Map.of("message", "user created successfully", "user_id", userId));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }
}
