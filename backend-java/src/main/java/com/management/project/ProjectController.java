package com.management.project;

import com.management.common.result.Result;
import com.management.project.dto.CreateProjectRequest;
import com.management.project.dto.UpdateProjectRequest;
import com.management.project.entity.Project;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public Result<List<Project>> list() {
        return Result.ok(projectService.listProjects());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Project> create(@Valid @RequestBody CreateProjectRequest req) {
        return Result.ok(projectService.createProject(req));
    }

    @GetMapping("/{id}")
    public Result<Project> get(@PathVariable Long id) {
        return Result.ok(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> update(@PathVariable Long id,
                                              @Valid @RequestBody UpdateProjectRequest req) {
        projectService.updateProject(id, req);
        return Result.ok(Map.of("message", "project updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.ok(Map.of("message", "project deleted"));
    }
}
