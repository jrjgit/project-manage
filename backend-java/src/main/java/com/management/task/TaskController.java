package com.management.task;

import com.management.common.result.Result;
import com.management.task.dto.AddTaskAssigneeRequest;
import com.management.task.dto.ChangeTaskStatusRequest;
import com.management.task.dto.CreateTaskRequest;
import com.management.task.dto.UpdateTaskRequest;
import com.management.task.entity.Task;
import com.management.task.entity.TaskAssignee;
import com.management.task.entity.TaskProgressHistory;
import com.management.task.entity.TaskStatusHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "任务管理", description = "任务的增删改查与状态流转")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    /**
     * 获取任务列表（支持按项目、状态、优先级、需求、迭代筛选）
     */
    @Operation(summary = "获取任务列表")
    @GetMapping
    public Result<List<Task>> list(@RequestParam(required = false) String projectId,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String priority,
                                    @RequestParam(name = "requirement_id", required = false) String requirementId,
                                    @RequestParam(name = "iteration_id", required = false) String iterationId) {
        return Result.ok(taskService.listTasks(projectId, status, priority, requirementId, iterationId));
    }

    /**
     * 创建任务
     */
    @Operation(summary = "创建任务")
    @PostMapping
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Task> create(@Valid @RequestBody CreateTaskRequest req) {
        return Result.ok(taskService.createTask(req));
    }

    /**
     * 获取任务详情
     */
    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskService.TaskDetail> get(@PathVariable Long id) {
        return Result.ok(taskService.getTask(id));
    }

    /**
     * 更新任务
     */
    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @RequestBody UpdateTaskRequest req) {
        return Result.ok(taskService.updateTask(id, req));
    }

    /**
     * 测试人员受理任务
     */
    @Operation(summary = "测试人员受理任务")
    @PostMapping("/{id}/accept")
    public Result<Task> accept(@PathVariable Long id) {
        return Result.ok(taskService.acceptTask(id));
    }

    /**
     * 变更任务状态
     */
    @Operation(summary = "变更任务状态")
    @PatchMapping("/{id}/status")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody ChangeTaskStatusRequest req) {
        taskService.changeStatus(id, req);
        return Result.ok(Map.of("message", "status changed successfully"));
    }

    /**
     * 获取任务状态历史
     */
    @Operation(summary = "获取任务状态历史")
    @GetMapping("/{id}/history")
    public Result<List<TaskStatusHistory>> history(@PathVariable Long id) {
        return Result.ok(taskService.getTaskHistory(id));
    }

    /**
     * 获取任务进度历史
     */
    @Operation(summary = "获取任务进度历史")
    @GetMapping("/{id}/progress-history")
    public Result<List<TaskProgressHistory>> progressHistory(@PathVariable Long id) {
        return Result.ok(taskService.getProgressHistory(id));
    }

    /**
     * 添加任务指派人
     */
    @Operation(summary = "添加任务指派人")
    @PostMapping("/{id}/assignees")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<TaskAssignee> addAssignee(@PathVariable Long id,
                                             @Valid @RequestBody AddTaskAssigneeRequest req) {
        return Result.ok(taskService.addAssignee(id, req));
    }

    /**
     * 移除任务指派人
     */
    @Operation(summary = "移除任务指派人")
    @DeleteMapping("/{id}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    public Result<Map<String, String>> removeAssignee(@PathVariable Long id,
                                                       @PathVariable Long userId) {
        taskService.removeAssignee(id, userId);
        return Result.ok(Map.of("message", "assignee removed"));
    }

    /**
     * 删除任务
     */
    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.ok(Map.of("message", "task deleted"));
    }
}
