package com.management.dashboard;

import com.management.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(dashboardService.getDashboard());
    }

    @GetMapping("/developer")
    @PreAuthorize("hasAnyRole('DEV','DEV_LEAD')")
    public Result<Map<String, Object>> developerDashboard() {
        return Result.ok(dashboardService.developerDashboard());
    }

    @GetMapping("/tester")
    @PreAuthorize("hasRole('TESTER')")
    public Result<Map<String, Object>> testerDashboard() {
        return Result.ok(dashboardService.testerDashboardV2());
    }
}
