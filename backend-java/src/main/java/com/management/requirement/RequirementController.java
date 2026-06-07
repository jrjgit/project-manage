package com.management.requirement;

import com.management.common.result.Result;
import com.management.common.exception.BusinessException;
import com.management.requirement.dto.CreateRequirementRequest;
import com.management.requirement.dto.UpdateRequirementRequest;
import com.management.requirement.dto.RequirementDetailDTO;
import com.management.requirement.entity.Requirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "需求管理", description = "需求的增删改查与状态流转")
@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {
    private final RequirementService requirementService;

    /**
     * 创建需求
     */
    @Operation(summary = "创建需求")
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Requirement> create(@Valid @RequestBody CreateRequirementRequest req) {
        return Result.ok(requirementService.create(req));
    }

    @Operation(summary = "获取需求列表")
    /**
     * 获取需求列表（支持按状态、系统、项目类型筛选）
     */
    @GetMapping
    public Result<List<Requirement>> list(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) String system,
                                           @RequestParam(required = false) String projectType) {
        return Result.ok(requirementService.list(status, system, projectType));
    }

    @Operation(summary = "获取运维需求列表")
    /**
     * 获取运维需求列表
     */
    @GetMapping("/ops")
    public Result<List<Requirement>> opsList() {
        return Result.ok(requirementService.opsList());
    }

    @Operation(summary = "获取项目需求列表")
    /**
     * 获取项目制需求列表
     */
    @GetMapping("/project")
    public Result<List<Requirement>> projectList() {
        return Result.ok(requirementService.projectList());
    }

    @Operation(summary = "获取需求详情")
    /**
     * 获取需求详情
     */
    @GetMapping("/{id}")
    public Result<RequirementDetailDTO> get(@PathVariable Long id) {
        return Result.ok(requirementService.getDetail(id));
    }

    @Operation(summary = "更新需求")
    /**
     * 更新需求
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Requirement> update(@PathVariable Long id,
                                       @RequestBody UpdateRequirementRequest req) {
        return Result.ok(requirementService.update(id, req));
    }

    @Operation(summary = "变更需求状态")
    /**
     * 变更需求状态
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new BusinessException(400, "状态不能为空");
        }
        requirementService.changeStatus(id, status);
        return Result.ok(Map.of("message", "status changed"));
    }

    @Operation(summary = "删除需求")
    /**
     * 删除需求
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        requirementService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }

    @Operation(summary = "指派开发组长")
    /**
     * 指派开发组长
     */
    @PutMapping("/{id}/assign-lead")
    @PreAuthorize("hasRole('PM') or hasRole('DEV_LEAD')")
    public Result<Map<String, String>> assignDevLead(@PathVariable Long id,
                                                       @RequestBody Map<String, Long> body) {
        Long devLeadId = body.get("dev_lead_id");
        if (devLeadId == null) {
            throw new BusinessException(400, "开发组长ID不能为空");
        }
        requirementService.assignDevLead(id, devLeadId);
        return Result.ok(Map.of("message", "dev lead assigned"));
    }

    @Operation(summary = "添加到迭代")
    /**
     * 将需求加入发布计划
     */
    @PutMapping("/{id}/release")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> addToRelease(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        String iterationId = body.get("iteration_id");
        if (iterationId == null || iterationId.isBlank()) {
            throw new BusinessException(400, "迭代ID不能为空");
        }
        requirementService.addToRelease(id, iterationId);
        return Result.ok(Map.of("message", "added to release"));
    }

    @Operation(summary = "从迭代移除")
    /**
     * 将需求移出发布计划
     */
    @DeleteMapping("/{id}/release")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> removeFromRelease(@PathVariable Long id) {
        requirementService.removeFromRelease(id);
        return Result.ok(Map.of("message", "removed from release"));
    }

    @Operation(summary = "上传需求文档")
    /**
     * 上传需求文档
     */
    @PostMapping("/{id}/document")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> uploadDocument(@PathVariable Long id,
                                                       @RequestParam("file") MultipartFile file) throws IOException {
        requirementService.uploadDocument(id, file);
        return Result.ok(Map.of("message", "document uploaded"));
    }

    @Operation(summary = "下载需求文档")
    /**
     * 下载需求文档
     */
    @GetMapping("/{id}/document")
    public void downloadDocument(@PathVariable Long id, HttpServletResponse response) throws IOException {
        requirementService.downloadDocument(id, response);
    }

    @Operation(summary = "删除需求文档")
    /**
     * 删除需求文档
     */
    @DeleteMapping("/{id}/document")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> deleteDocument(@PathVariable Long id) {
        requirementService.deleteDocument(id);
        return Result.ok(Map.of("message", "document deleted"));
    }
}
