package com.management.iteration;

import com.management.common.result.Result;
import com.management.iteration.entity.Iteration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iterations")
@RequiredArgsConstructor
public class IterationController {
    private final IterationService iterationService;

    @GetMapping
    public Result<List<Iteration>> list() {
        return Result.ok(iterationService.list());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Iteration> create(@RequestBody Iteration req) {
        return Result.ok(iterationService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Iteration> update(@PathVariable Long id, @RequestBody Iteration req) {
        return Result.ok(iterationService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        iterationService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
