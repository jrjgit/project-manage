package com.management.dictionary;

import com.management.common.result.Result;
import com.management.dictionary.entity.Dictionary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;

    @GetMapping
    public Result<List<Dictionary>> list(@RequestParam(required = false) String type) {
        if (type != null) return Result.ok(dictionaryService.listByType(type));
        return Result.ok(dictionaryService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Dictionary> create(@RequestBody Dictionary dict) {
        return Result.ok(dictionaryService.create(dict));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Dictionary> update(@PathVariable Long id, @RequestBody Dictionary dict) {
        return Result.ok(dictionaryService.update(id, dict));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        dictionaryService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
