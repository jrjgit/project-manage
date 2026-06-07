package com.management.system;

import com.management.common.result.Result;
import com.management.system.entity.SystemInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统管理", description = "系统信息的增删改查")
@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
public class SystemInfoController {
    private final SystemInfoService systemInfoService;

    @Operation(summary = "获取系统列表")
    /**
     * 获取系统列表
     */
    @GetMapping
    public Result<List<SystemInfo>> list() {
        return Result.ok(systemInfoService.list());
    }

    @Operation(summary = "获取系统详情")
    /**
     * 获取系统详情
     */
    @GetMapping("/{id}")
    public Result<SystemInfo> get(@PathVariable Long id) {
        return Result.ok(systemInfoService.get(id));
    }

    @Operation(summary = "创建系统信息")
    /**
     * 创建系统
     */
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<SystemInfo> create(@RequestBody SystemInfo req) {
        return Result.ok(systemInfoService.create(req));
    }

    @Operation(summary = "更新系统信息")
    /**
     * 更新系统
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<SystemInfo> update(@PathVariable Long id, @RequestBody SystemInfo req) {
        return Result.ok(systemInfoService.update(id, req));
    }

    @Operation(summary = "删除系统信息")
    /**
     * 删除系统
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        systemInfoService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
