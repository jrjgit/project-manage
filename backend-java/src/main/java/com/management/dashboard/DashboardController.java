package com.management.dashboard;

import com.management.common.result.Result;
import lombok.RequiredArgsConstructor;
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
}
