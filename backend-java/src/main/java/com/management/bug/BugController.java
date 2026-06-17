package com.management.bug;

import com.management.bug.dto.*;
import com.management.bug.entity.*;
import com.management.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "Bug管理", description = "Bug的增删改查与状态流转")
@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {
    private final BugService bugService;

    /**
     * 获取Bug列表（支持按任务、状态、严重程度筛选）
     */
    @Operation(summary = "获取Bug列表")
    @GetMapping
    public Result<List<Bug>> list(@RequestParam(name = "task_id", required = false) String taskId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String severity) {
        return Result.ok(bugService.listBugs(taskId, status, severity));
    }

    /**
     * 创建Bug
     */
    @Operation(summary = "创建Bug")
    @PostMapping
    @PreAuthorize("hasAnyRole('TESTER','PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Bug> create(@Valid @RequestBody CreateBugRequest req) {
        return Result.ok(bugService.createBug(req));
    }

    /**
     * 获取Bug详情
     */
    @Operation(summary = "获取Bug详情")
    @GetMapping("/{id}")
    public Result<Bug> get(@PathVariable Long id) {
        return Result.ok(bugService.getBug(id));
    }

    /**
     * 更新Bug
     */
    @Operation(summary = "更新Bug")
    @PutMapping("/{id}")
    public Result<Bug> update(@PathVariable Long id, @RequestBody UpdateBugRequest req) {
        return Result.ok(bugService.updateBug(id, req));
    }

    /**
     * 变更Bug状态
     */
    @Operation(summary = "变更Bug状态")
    @PatchMapping("/{id}/status")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody ChangeBugStatusRequest req) {
        bugService.changeStatus(id, req);
        return Result.ok(Map.of("message", "status changed successfully"));
    }

    /**
     * 获取Bug状态历史
     */
    @Operation(summary = "获取Bug状态历史")
    @GetMapping("/{id}/history")
    public Result<List<BugStatusHistory>> history(@PathVariable Long id) {
        return Result.ok(bugService.getBugHistory(id));
    }

    /**
     * 上传Bug截图
     */
    @Operation(summary = "上传Bug图片")
    @PostMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('TESTER','PM')")
    public Result<Map<String, String>> uploadImage(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        bugService.uploadImage(id, file);
        return Result.ok(Map.of("message", "image uploaded"));
    }

    /**
     * 获取Bug所有截图列表
     */
    @Operation(summary = "获取Bug所有截图列表")
    @GetMapping("/{id}/images")
    public Result<List<BugImage>> listImages(@PathVariable Long id) {
        return Result.ok(bugService.listImages(id));
    }

    /**
     * 下载指定Bug截图
     */
    @Operation(summary = "下载指定Bug截图")
    @GetMapping("/{id}/images/{imageId}")
    public void downloadImage(@PathVariable Long id, @PathVariable Long imageId,
                              HttpServletResponse response) throws IOException {
        bugService.downloadImage(id, imageId, response);
    }

    /**
     * 删除指定Bug截图
     */
    @Operation(summary = "删除指定Bug截图")
    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAnyRole('TESTER','PM')")
    public Result<Map<String, String>> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        bugService.deleteImageById(id, imageId);
        return Result.ok(Map.of("message", "image deleted"));
    }
}
