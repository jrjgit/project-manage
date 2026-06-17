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

    @Operation(summary = "运维营收统计")
    /**
     * 获取运维营收统计
     */
    @GetMapping("/revenue")
    public Result<Map<String, Object>> revenue(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(required = false) Long projectId) {
        return Result.ok(statisticsService.revenueStats(year, projectId));
    }

    @Operation(summary = "绩效统计")
    /**
     * 获取绩效统计
     */
    @GetMapping("/performance")
    public Result<Map<String, Object>> performance(
            @RequestParam(defaultValue = "year") String filterType,
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(statisticsService.performanceStats(filterType, year, month, startDate, endDate));
    }
}
