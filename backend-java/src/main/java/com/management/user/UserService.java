package com.management.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.group.entity.Group;
import com.management.group.mapper.GroupMapper;
import com.management.iteration.entity.Iteration;
import com.management.iteration.mapper.IterationMapper;
import com.management.project.entity.Project;
import com.management.project.mapper.ProjectMapper;
import com.management.system.entity.SystemInfo;
import com.management.system.mapper.SystemInfoMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final IterationMapper iterationMapper;
    private final SystemInfoMapper systemInfoMapper;
    private final GroupMapper groupMapper;

    public JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<User> listUsers() {
        JwtUserDetails user = currentUser();
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if ("tester".equals(user.getRole()) || "tester_lead".equals(user.getRole())) {
            q.eq(User::getRole, "dev");
        }
        return userMapper.selectList(q);
    }

    public User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        return user;
    }

    public void updateRole(Long id, String role, Long groupId) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        user.setRole(role);
        if (groupId != null) {
            user.setGroupId(groupId);
        }
        userMapper.updateById(user);
    }

    public void deleteUser(Long id) {
        JwtUserDetails current = currentUser();
        if (current.getUserId().equals(id)) {
            throw new BusinessException(400, "不能删除自己");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        projectMapper.update(null, new LambdaUpdateWrapper<Project>()
                .eq(Project::getPmId, id).set(Project::getPmId, null));
        projectMapper.update(null, new LambdaUpdateWrapper<Project>()
                .eq(Project::getCreatedBy, id).set(Project::getCreatedBy, null));
        iterationMapper.update(null, new LambdaUpdateWrapper<Iteration>()
                .eq(Iteration::getCreatedBy, id).set(Iteration::getCreatedBy, null));
        systemInfoMapper.update(null, new LambdaUpdateWrapper<SystemInfo>()
                .eq(SystemInfo::getCreatedBy, id).set(SystemInfo::getCreatedBy, null));
        groupMapper.update(null, new LambdaUpdateWrapper<Group>()
                .eq(Group::getPmId, id).set(Group::getPmId, null));
        groupMapper.update(null, new LambdaUpdateWrapper<Group>()
                .eq(Group::getDevLeadId, id).set(Group::getDevLeadId, null));
        userMapper.deleteById(id);
    }
}
