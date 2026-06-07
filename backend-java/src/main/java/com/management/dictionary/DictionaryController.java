package com.management.dictionary;

import com.management.common.result.Result;
import com.management.dictionary.entity.Dictionary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "字典管理", description = "字典数据的维护")
@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;

    @Operation(summary = "获取字典列表")
    /**
     * 获取字典列表（可按类型筛选）
     */
    @GetMapping
    public Result<List<Dictionary>> list(@RequestParam(required = false) String type) {
        if (type != null) return Result.ok(dictionaryService.listByType(type));
        return Result.ok(dictionaryService.listAll());
    }

    @Operation(summary = "创建字典项")
    /**
     * 创建字典条目
     */
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Dictionary> create(@RequestBody Dictionary dict) {
        return Result.ok(dictionaryService.create(dict));
    }

    @Operation(summary = "更新字典项")
    /**
     * 更新字典条目
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Dictionary> update(@PathVariable Long id, @RequestBody Dictionary dict) {
        return Result.ok(dictionaryService.update(id, dict));
    }

    @Operation(summary = "删除字典项")
    /**
     * 删除字典条目
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        dictionaryService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
