package com.management.user;

import com.management.common.result.Result;
import com.management.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD','DEV','TESTER','TESTER_LEAD')")
    public Result<List<User>> listUsers() {
        return Result.ok(userService.listUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.ok(userService.getUser(id));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> updateRole(@PathVariable Long id,
                                                   @RequestBody UpdateRoleRequest req) {
        userService.updateRole(id, req.getRole(), req.getGroupId());
        return Result.ok(Map.of("message", "user updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok(Map.of("message", "user deleted"));
    }

    @Data
    static class UpdateRoleRequest {
        @NotBlank
        private String role;
        private Long groupId;
    }
}
