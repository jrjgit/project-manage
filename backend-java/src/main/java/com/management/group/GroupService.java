package com.management.group;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.group.dto.CreateGroupRequest;
import com.management.group.dto.UpdateGroupRequest;
import com.management.group.entity.Group;
import com.management.group.mapper.GroupMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<Map<String, Object>> listGroups() {
        List<Group> groups = groupMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Group g : groups) {
            g.setDevLead(userMapper.selectById(g.getDevLeadId()));
            String expectedRole = "test".equals(g.getLeadRole()) ? "tester" : "dev";
            long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getGroupId, g.getId())
                    .eq(User::getRole, expectedRole));
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("name", g.getName());
            item.put("pm_id", g.getPmId());
            item.put("dev_lead_id", g.getDevLeadId());
            item.put("lead_role", g.getLeadRole());
            item.put("created_at", g.getCreatedAt());
            item.put("dev_lead", g.getDevLead());
            item.put("member_count", count);
            result.add(item);
        }
        return result;
    }

    public Group createGroup(CreateGroupRequest req) {
        Long userId = currentUser().getUserId();
        String leadRole = req.getLeadRole() != null ? req.getLeadRole() : "dev";
        if (userMapper.selectById(req.getDevLeadId()) == null)
            throw new BusinessException(400, "开发组长不存在");

        Group group = new Group();
        group.setName(req.getName());
        group.setPmId(userId);
        group.setDevLeadId(req.getDevLeadId());
        group.setLeadRole(leadRole);
        groupMapper.insert(group);

        User devLead = userMapper.selectById(req.getDevLeadId());
        if (devLead != null) {
            devLead.setGroupId(group.getId());
            userMapper.updateById(devLead);
        }
        log.info("Group created: name={}, id={}, leadRole={}", group.getName(), group.getId(), leadRole);
        return group;
    }

    public Map<String, Object> getGroup(Long id) {
        Group group = groupMapper.selectById(id);
        if (group == null) throw new BusinessException(404, "group not found");
        group.setDevLead(userMapper.selectById(group.getDevLeadId()));
        List<User> members = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, id));
        return Map.of("group", group, "members", members);
    }

    public void updateGroup(Long id, UpdateGroupRequest req) {
        Group oldGroup = groupMapper.selectById(id);
        if (oldGroup == null) throw new BusinessException(404, "group not found");

        if (req.getDevLeadId() != null && !req.getDevLeadId().equals(oldGroup.getDevLeadId())) {
            User oldLead = userMapper.selectById(oldGroup.getDevLeadId());
            if (oldLead != null) {
                oldLead.setGroupId(null);
                userMapper.updateById(oldLead);
            }
            User newLead = userMapper.selectById(req.getDevLeadId());
            if (newLead != null) {
                newLead.setGroupId(id);
                userMapper.updateById(newLead);
            }
        }
        oldGroup.setName(req.getName());
        if (req.getDevLeadId() != null) oldGroup.setDevLeadId(req.getDevLeadId());
        if (req.getLeadRole() != null) oldGroup.setLeadRole(req.getLeadRole());
        groupMapper.updateById(oldGroup);
    }

    public void deleteGroup(Long id) {
        User update = new User();
        update.setGroupId(null);
        userMapper.update(update, new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, id));
        groupMapper.deleteById(id);
    }

    public void addMember(Long groupId, Long userId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) throw new BusinessException(404, "group not found");
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "user not found");
        String expectedRole = "test".equals(group.getLeadRole()) ? "tester" : "dev";
        if (!expectedRole.equals(user.getRole())) {
            throw new BusinessException("member role does not match group type");
        }
        user.setGroupId(groupId);
        userMapper.updateById(user);
    }

    public void removeMember(Long groupId, Long userId) {
        User update = new User();
        update.setId(userId);
        update.setGroupId(null);
        userMapper.update(update, new LambdaQueryWrapper<User>()
                .eq(User::getId, userId).eq(User::getGroupId, groupId));
    }

    public Map<String, Object> getMyTeam() {
        JwtUserDetails u = currentUser();
        Group group = groupMapper.selectOne(new LambdaQueryWrapper<Group>()
                .eq(Group::getDevLeadId, u.getUserId()));
        if (group == null) {
            return Map.of("group", null, "members", List.of());
        }
        List<User> members = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, group.getId()));
        return Map.of("group", group, "members", members);
    }
}
