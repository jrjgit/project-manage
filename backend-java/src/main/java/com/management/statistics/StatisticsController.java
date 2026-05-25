package com.management.statistics;

import com.management.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/revenue")
    public Result<Map<String, Object>> revenue(@RequestParam(defaultValue = "2026") int year) {
        return Result.ok(statisticsService.revenueStats(year));
    }

    @GetMapping("/performance")
    public Result<Map<String, Object>> performance(@RequestParam int year, @RequestParam int month) {
        return Result.ok(statisticsService.performanceStats(year, month));
    }
}
