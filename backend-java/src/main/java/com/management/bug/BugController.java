package com.management.bug;

import com.management.bug.dto.*;
import com.management.bug.entity.*;
import com.management.common.result.Result;
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

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {
    private final BugService bugService;

    @GetMapping
    public Result<List<Bug>> list(@RequestParam(name = "task_id", required = false) String taskId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String severity) {
        return Result.ok(bugService.listBugs(taskId, status, severity));
    }

    @PostMapping
    @PreAuthorize("hasRole('TESTER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Bug> create(@Valid @RequestBody CreateBugRequest req) {
        return Result.ok(bugService.createBug(req));
    }

    @GetMapping("/{id}")
    public Result<Bug> get(@PathVariable Long id) {
        return Result.ok(bugService.getBug(id));
    }

    @PutMapping("/{id}")
    public Result<Bug> update(@PathVariable Long id, @RequestBody UpdateBugRequest req) {
        return Result.ok(bugService.updateBug(id, req));
    }

    @PatchMapping("/{id}/status")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody ChangeBugStatusRequest req) {
        bugService.changeStatus(id, req);
        return Result.ok(Map.of("message", "status changed successfully"));
    }

    @GetMapping("/{id}/history")
    public Result<List<BugStatusHistory>> history(@PathVariable Long id) {
        return Result.ok(bugService.getBugHistory(id));
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('TESTER','PM')")
    public Result<Map<String, String>> uploadImage(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        bugService.uploadImage(id, file);
        return Result.ok(Map.of("message", "image uploaded"));
    }

    @GetMapping("/{id}/image")
    public void downloadImage(@PathVariable Long id, HttpServletResponse response) throws IOException {
        bugService.downloadImage(id, response);
    }

    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('TESTER','PM')")
    public Result<Map<String, String>> deleteImage(@PathVariable Long id) {
        bugService.deleteImage(id);
        return Result.ok(Map.of("message", "image deleted"));
    }
}
