package com.management.requirement;

import com.management.common.result.Result;
import com.management.requirement.dto.CreateFeatureRequest;
import com.management.requirement.dto.UpdateFeatureRequest;
import com.management.requirement.entity.Feature;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FeatureController {
    private final FeatureService featureService;

    @GetMapping("/api/requirements/{requirementId}/features")
    public Result<List<Feature>> listByRequirement(@PathVariable Long requirementId) {
        return Result.ok(featureService.listByRequirement(requirementId));
    }

    @PostMapping("/api/requirements/{requirementId}/features")
    @PreAuthorize("hasAnyRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Feature> create(@PathVariable Long requirementId,
                                   @Valid @RequestBody CreateFeatureRequest req) {
        return Result.ok(featureService.create(requirementId, req));
    }

    @PutMapping("/api/features/{id}")
    @PreAuthorize("hasAnyRole('PM')")
    public Result<Feature> update(@PathVariable Long id,
                                   @RequestBody UpdateFeatureRequest req) {
        return Result.ok(featureService.update(id, req));
    }

    @PatchMapping("/api/features/{id}/status")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        featureService.changeStatus(id, body.get("status"));
        return Result.ok(Map.of("message", "status changed"));
    }

    @DeleteMapping("/api/features/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        featureService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
