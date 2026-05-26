package com.management.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.project.dto.CreateProjectRequest;
import com.management.project.dto.UpdateProjectRequest;
import com.management.project.entity.Project;
import com.management.project.mapper.ProjectMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<Project> listProjects() {
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().orderByDesc(Project::getCreatedAt));
        for (Project p : projects) {
            fillAssociations(p);
        }
        return projects;
    }

    public Project createProject(CreateProjectRequest req) {
        Long userId = currentUser().getUserId();
        Project project = new Project();
        project.setName(req.getName());
        project.setCode(req.getCode());
        project.setProjectType(req.getProjectType());
        project.setSystemScope(req.getSystemScope() != null
                ? req.getSystemScope().stream().map(String::valueOf).collect(Collectors.joining(","))
                : null);
        project.setHrScope(req.getHrScope() != null
                ? req.getHrScope().stream().map(String::valueOf).collect(Collectors.joining(","))
                : null);
        project.setPmId(userId);
        project.setCreatedBy(userId);
        projectMapper.insert(project);
        fillAssociations(project);
        return project;
    }

    public Project getProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        fillAssociations(project);
        return project;
    }

    public void updateProject(Long id, UpdateProjectRequest req) {
        Project project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        if (req.getName() != null) project.setName(req.getName());
        if (req.getCode() != null) project.setCode(req.getCode());
        if (req.getProjectType() != null) project.setProjectType(req.getProjectType());
        if (req.getSystemScope() != null)
            project.setSystemScope(req.getSystemScope().stream().map(String::valueOf).collect(Collectors.joining(",")));
        if (req.getHrScope() != null)
            project.setHrScope(req.getHrScope().stream().map(String::valueOf).collect(Collectors.joining(",")));
        if (req.getPmId() != null) {
            if (userMapper.selectById(req.getPmId()) == null)
                throw new BusinessException(400, "项目经理不存在");
            project.setPmId(req.getPmId());
        }
        projectMapper.updateById(project);
    }

    public void deleteProject(Long id) {
        projectMapper.deleteById(id);
    }

    private void fillAssociations(Project p) {
        if (p.getPmId() != null) p.setPm(userMapper.selectById(p.getPmId()));
        if (p.getCreatedBy() != null) p.setCreator(userMapper.selectById(p.getCreatedBy()));
        if (p.getHrScope() != null && !p.getHrScope().isBlank()) {
            List<Long> ids = Arrays.stream(p.getHrScope().split(","))
                    .filter(StringUtils::hasText)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            p.setHrUsers(userMapper.selectBatchIds(ids));
        }
    }
}
