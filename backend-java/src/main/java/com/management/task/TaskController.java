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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public Result<List<Task>> list(@RequestParam(required = false) String projectId,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String priority,
                                    @RequestParam(required = false) String requirementId) {
        return Result.ok(taskService.listTasks(projectId, status, priority, requirementId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Task> create(@Valid @RequestBody CreateTaskRequest req) {
        return Result.ok(taskService.createTask(req));
    }

    @GetMapping("/{id}")
    public Result<TaskService.TaskDetail> get(@PathVariable Long id) {
        return Result.ok(taskService.getTask(id));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @RequestBody UpdateTaskRequest req) {
        return Result.ok(taskService.updateTask(id, req));
    }

    @PatchMapping("/{id}/status")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody ChangeTaskStatusRequest req) {
        taskService.changeStatus(id, req);
        return Result.ok(Map.of("message", "status changed successfully"));
    }

    @GetMapping("/{id}/history")
    public Result<List<TaskStatusHistory>> history(@PathVariable Long id) {
        return Result.ok(taskService.getTaskHistory(id));
    }

    @GetMapping("/{id}/progress-history")
    public Result<List<TaskProgressHistory>> progressHistory(@PathVariable Long id) {
        return Result.ok(taskService.getProgressHistory(id));
    }

    @PostMapping("/{id}/assignees")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<TaskAssignee> addAssignee(@PathVariable Long id,
                                             @Valid @RequestBody AddTaskAssigneeRequest req) {
        return Result.ok(taskService.addAssignee(id, req));
    }

    @DeleteMapping("/{id}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    public Result<Map<String, String>> removeAssignee(@PathVariable Long id,
                                                       @PathVariable Long userId) {
        taskService.removeAssignee(id, userId);
        return Result.ok(Map.of("message", "assignee removed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.ok(Map.of("message", "task deleted"));
    }
}
