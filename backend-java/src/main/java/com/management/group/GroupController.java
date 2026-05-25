package com.management.group;

import com.management.common.result.Result;
import com.management.group.dto.AddMemberRequest;
import com.management.group.dto.CreateGroupRequest;
import com.management.group.dto.UpdateGroupRequest;
import com.management.group.entity.Group;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(groupService.listGroups());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Group> create(@Valid @RequestBody CreateGroupRequest req) {
        return Result.ok(groupService.createGroup(req));
    }

    @GetMapping("/my-team")
    @PreAuthorize("hasRole('DEV_LEAD')")
    public Result<Map<String, Object>> myTeam() {
        return Result.ok(groupService.getMyTeam());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.ok(groupService.getGroup(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateGroupRequest req) {
        groupService.updateGroup(id, req);
        return Result.ok(Map.of("message", "group updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return Result.ok(Map.of("message", "group deleted"));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> addMember(@PathVariable Long id,
                                                  @Valid @RequestBody AddMemberRequest req) {
        groupService.addMember(id, req.getUserId());
        return Result.ok(Map.of("message", "member added"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> removeMember(@PathVariable Long id,
                                                     @PathVariable Long userId) {
        groupService.removeMember(id, userId);
        return Result.ok(Map.of("message", "member removed"));
    }
}
