package com.management.user;

import com.management.common.result.Result;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户增删改查")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 获取用户列表
     */
    @Operation(summary = "获取用户列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD','DEV','TESTER')")
    public Result<List<User>> listUsers() {
        return Result.ok(userService.listUsers());
    }

    /**
     * 获取单个用户
     */
    @Operation(summary = "获取单个用户")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.ok(userService.getUser(id));
    }

    /**
     * 更新用户角色
     */
    @Operation(summary = "更新用户角色")
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> updateRole(@PathVariable Long id,
                                                   @RequestBody UpdateRoleRequest req) {
        userService.updateRole(id, req.getRole());
        return Result.ok(Map.of("message", "user updated"));
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        return Result.ok(userService.updateUser(id, req));
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok(Map.of("message", "user deleted"));
    }

    @Operation(summary = "获取开发人员工作负载（各状态任务数）")
    @GetMapping("/workload")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Map<String, Object>>> getUserWorkload() {
        return Result.ok(userService.getUserWorkload());
    }

    /**
     * 修改用户密码
     */
    @Operation(summary = "修改用户密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> changePassword(@PathVariable Long id,
                                                       @RequestBody ChangePasswordRequest req) {
        userService.changePassword(id, req.getPassword());
        return Result.ok(Map.of("message", "password changed"));
    }

    @Data
    static class UpdateRoleRequest {
        @NotBlank
        private String role;
    }

    @Data
    static class ChangePasswordRequest {
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    static class UpdateUserRequest {
        @NotBlank(message = "不能为空")
        private String name;
        @NotBlank(message = "不能为空")
        private String account;
        @Email(message = "邮箱格式不正确")
        private String email;
        private String skills;
    }
}
