package com.management.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.bug.entity.Bug;
import com.management.bug.mapper.BugMapper;
import com.management.common.constant.UserRole;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.management.project.entity.Project;
import com.management.project.mapper.ProjectMapper;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;
    private final BugMapper bugMapper;
    private final RequirementMapper requirementMapper;
    private final PasswordEncoder passwordEncoder;

    public JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<User> listUsers() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByAsc(User::getId));
    }

    public User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "user not found");
        return user;
    }

    public void updateRole(Long id, String role) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "user not found");
        user.setRole(role);
        userMapper.updateById(user);
    }

    public User updateUser(Long id, com.management.user.UserController.UpdateUserRequest req) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "user not found");
        if (req.getName() != null) user.setName(req.getName());
        if (req.getAccount() != null) user.setAccount(req.getAccount());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getSkills() != null) user.setSkills(req.getSkills());
        userMapper.updateById(user);
        return user;
    }

    public void deleteUser(Long id) {
        JwtUserDetails current = currentUser();
        if (current.getUserId().equals(id)) throw new BusinessException(400, "不能删除自己");
        if (userMapper.selectById(id) == null) throw new BusinessException(404, "user not found");

        List<String> refs = new ArrayList<>();

        long c1 = projectMapper.selectCount(new LambdaQueryWrapper<Project>().eq(Project::getPmId, id));
        if (c1 > 0) refs.add(c1 + " 个项目(作为项目经理)");
        long c2 = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getAssigneeId, id));
        if (c2 > 0) refs.add(c2 + " 个任务(作为指派人)");
        long c3 = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getDevLeadId, id));
        if (c3 > 0) refs.add(c3 + " 个任务(作为开发组长)");
        long c5 = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getTesterId, id));
        if (c5 > 0) refs.add(c5 + " 个任务(作为测试人员)");
        long c6 = requirementMapper.selectCount(new LambdaQueryWrapper<Requirement>().eq(Requirement::getPersonId, id));
        if (c6 > 0) refs.add(c6 + " 个需求(作为业务负责人)");
        long c7 = requirementMapper.selectCount(new LambdaQueryWrapper<Requirement>().eq(Requirement::getDevLeadId, id));
        if (c7 > 0) refs.add(c7 + " 个需求(作为开发组长)");

        if (!refs.isEmpty()) {
            throw new BusinessException(400,
                    "该用户存在以下关联数据，请先解除关联后再删除：\n " + String.join("\n ", refs));
        }
        userMapper.deleteById(id);
    }

    public void changePassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(400, "密码长度不能少于 6 位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /**
     * 当前用户修改自己的密码，需验证旧密码
     */
    public void changeOwnPassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new BusinessException(400, "请输入原密码");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(400, "新密码长度不能少于 6 位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public List<Map<String, Object>> getUserWorkload() {
        List<User> devUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>().in(User::getRole, UserRole.DEV, UserRole.DEV_LEAD));
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : devUsers) {
            long devCount = taskMapper.selectCount(
                    new LambdaQueryWrapper<Task>().eq(Task::getAssigneeId, u.getId()));
            long bugCount = bugMapper.selectCount(
                    new LambdaQueryWrapper<Bug>().eq(Bug::getAssigneeId, u.getId())
                            .ne(Bug::getStatus, "closed"));
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("userId", u.getId());
            item.put("devCount", devCount);
            item.put("bugCount", bugCount);
            result.add(item);
        }
        return result;
    }
}
