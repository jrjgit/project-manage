package com.management.project;

import com.management.common.result.Result;
import com.management.project.dto.CreateProjectRequest;
import com.management.project.dto.UpdateProjectRequest;
import com.management.project.entity.Project;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "项目管理", description = "项目的增删改查")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    /**
     * 获取项目列表
     */
    @Operation(summary = "获取项目列表")
    @GetMapping
    public Result<List<Project>> list() {
        return Result.ok(projectService.listProjects());
    }

    /**
     * 创建项目
     */
    @Operation(summary = "创建项目")
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Project> create(@Valid @RequestBody CreateProjectRequest req) {
        return Result.ok(projectService.createProject(req));
    }

    /**
     * 获取项目详情
     */
    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<Project> get(@PathVariable Long id) {
        return Result.ok(projectService.getProject(id));
    }

    /**
     * 更新项目
     */
    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateProjectRequest req) {
        projectService.updateProject(id, req);
        return Result.ok(Map.of("message", "project updated"));
    }

    /**
     * 删除项目
     */
    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.ok(Map.of("message", "project deleted"));
    }
}
