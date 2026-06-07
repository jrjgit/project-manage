package com.management.iteration;

import com.management.common.result.Result;
import com.management.iteration.entity.Iteration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "迭代管理", description = "迭代/版本的增删改查")
@RestController
@RequestMapping("/api/iterations")
@RequiredArgsConstructor
public class IterationController {
    private final IterationService iterationService;

    @Operation(summary = "获取迭代列表")
    /**
     * 获取迭代列表
     */
    @GetMapping
    public Result<List<Iteration>> list() {
        return Result.ok(iterationService.list());
    }

    @Operation(summary = "创建迭代")
    /**
     * 创建迭代
     */
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Iteration> create(@RequestBody Iteration req) {
        return Result.ok(iterationService.create(req));
    }

    @Operation(summary = "更新迭代")
    /**
     * 更新迭代
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Iteration> update(@PathVariable Long id, @RequestBody Iteration req) {
        return Result.ok(iterationService.update(id, req));
    }

    @Operation(summary = "删除迭代")
    /**
     * 删除迭代
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        iterationService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
