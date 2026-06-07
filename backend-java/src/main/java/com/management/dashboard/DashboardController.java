package com.management.dashboard;

import com.management.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "仪表盘", description = "首页综合仪表盘数据")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "获取综合仪表盘数据")
    /**
     * 获取PM仪表盘数据
     */
    @GetMapping
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(dashboardService.getDashboard());
    }

    @Operation(summary = "获取开发者仪表盘数据")
    /**
     * 获取开发者仪表盘数据
     */
    @GetMapping("/developer")
    @PreAuthorize("hasAnyRole('DEV','DEV_LEAD')")
    public Result<Map<String, Object>> developerDashboard() {
        return Result.ok(dashboardService.developerDashboard());
    }

    @Operation(summary = "获取测试者仪表盘数据")
    /**
     * 获取测试人员仪表盘数据
     */
    @GetMapping("/tester")
    @PreAuthorize("hasRole('TESTER')")
    public Result<Map<String, Object>> testerDashboard() {
        return Result.ok(dashboardService.testerDashboardV2());
    }
}
