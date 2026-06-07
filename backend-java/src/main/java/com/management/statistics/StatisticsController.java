package com.management.statistics;

import com.management.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "数据统计", description = "统计和仪表盘数据")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Operation(summary = "收入统计")
    /**
     * 获取营收统计
     */
    @GetMapping("/revenue")
    public Result<Map<String, Object>> revenue(@RequestParam(defaultValue = "2026") int year) {
        return Result.ok(statisticsService.revenueStats(year));
    }

    @Operation(summary = "绩效统计")
    /**
     * 获取绩效统计
     */
    @GetMapping("/performance")
    public Result<Map<String, Object>> performance(@RequestParam int year, @RequestParam int month) {
        return Result.ok(statisticsService.performanceStats(year, month));
    }
}
