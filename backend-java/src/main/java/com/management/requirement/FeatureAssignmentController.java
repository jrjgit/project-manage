package com.management.requirement;

import com.management.common.result.Result;
import com.management.requirement.dto.CreateFeatureAssignmentRequest;
import com.management.requirement.entity.FeatureAssignment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FeatureAssignmentController {
    private final FeatureAssignmentService assignmentService;

    @GetMapping("/api/features/{featureId}/assignments")
    public Result<List<FeatureAssignment>> listByFeature(@PathVariable Long featureId) {
        return Result.ok(assignmentService.listByFeature(featureId));
    }

    @PostMapping("/api/features/{featureId}/assignments")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<FeatureAssignment> create(@PathVariable Long featureId,
                                             @Valid @RequestBody CreateFeatureAssignmentRequest req) {
        req.setFeatureId(featureId);
        return Result.ok(assignmentService.create(req));
    }

    @DeleteMapping("/api/feature-assignments/{id}")
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
