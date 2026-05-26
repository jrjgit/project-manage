package com.management.system;

import com.management.common.result.Result;
import com.management.system.entity.SystemInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
public class SystemInfoController {
    private final SystemInfoService systemInfoService;

    @GetMapping
    public Result<List<SystemInfo>> list() {
        return Result.ok(systemInfoService.list());
    }

    @GetMapping("/{id}")
    public Result<SystemInfo> get(@PathVariable Long id) {
        return Result.ok(systemInfoService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<SystemInfo> create(@RequestBody SystemInfo req) {
        return Result.ok(systemInfoService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<SystemInfo> update(@PathVariable Long id, @RequestBody SystemInfo req) {
        return Result.ok(systemInfoService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        systemInfoService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
