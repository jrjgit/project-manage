package com.management.requirement;

import com.management.common.result.Result;
import com.management.requirement.dto.CreateRequirementRequest;
import com.management.requirement.dto.UpdateRequirementRequest;
import com.management.requirement.dto.RequirementDetailDTO;
import com.management.requirement.entity.Requirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {
    private final RequirementService requirementService;

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Requirement> create(@Valid @RequestBody CreateRequirementRequest req) {
        return Result.ok(requirementService.create(req));
    }

    @GetMapping
    public Result<List<Requirement>> list(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) String system,
                                           @RequestParam(required = false) String projectType,
                                           @RequestParam(required = false) String source) {
        return Result.ok(requirementService.list(status, system, projectType, source));
    }

    @GetMapping("/ops")
    public Result<List<Requirement>> opsList() {
        return Result.ok(requirementService.opsList());
    }

    @GetMapping("/project")
    public Result<List<Requirement>> projectList() {
        return Result.ok(requirementService.projectList());
    }

    @GetMapping("/{id}")
    public Result<RequirementDetailDTO> get(@PathVariable Long id) {
        return Result.ok(requirementService.getDetail(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Requirement> update(@PathVariable Long id,
                                       @RequestBody UpdateRequirementRequest req) {
        return Result.ok(requirementService.update(id, req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        requirementService.changeStatus(id, body.get("status"));
        return Result.ok(Map.of("message", "status changed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        requirementService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }

    @PutMapping("/{id}/assign-lead")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> assignDevLead(@PathVariable Long id,
                                                      @RequestBody Map<String, Long> body) {
        requirementService.assignDevLead(id, body.get("dev_lead_id"));
        return Result.ok(Map.of("message", "dev lead assigned"));
    }

    @PutMapping("/{id}/release")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> addToRelease(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        requirementService.addToRelease(id, body.get("iteration_id"));
        return Result.ok(Map.of("message", "added to release"));
    }

    @DeleteMapping("/{id}/release")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> removeFromRelease(@PathVariable Long id) {
        requirementService.removeFromRelease(id);
        return Result.ok(Map.of("message", "removed from release"));
    }

    @PostMapping("/{id}/document")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> uploadDocument(@PathVariable Long id,
                                                       @RequestParam("file") MultipartFile file) throws IOException {
        requirementService.uploadDocument(id, file);
        return Result.ok(Map.of("message", "document uploaded"));
    }

    @GetMapping("/{id}/document")
    public void downloadDocument(@PathVariable Long id, HttpServletResponse response) throws IOException {
        requirementService.downloadDocument(id, response);
    }

    @DeleteMapping("/{id}/document")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> deleteDocument(@PathVariable Long id) {
        requirementService.deleteDocument(id);
        return Result.ok(Map.of("message", "document deleted"));
    }
}
