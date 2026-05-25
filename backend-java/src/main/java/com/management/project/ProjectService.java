package com.management.project;

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

import java.util.List;

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
        List<Project> projects = projectMapper.selectList(null);
        for (Project p : projects) {
            p.setPm(userMapper.selectById(p.getPmId()));
        }
        return projects;
    }

    public Project createProject(CreateProjectRequest req) {
        Long userId = currentUser().getUserId();
        Project project = new Project();
        project.setName(req.getName());
        project.setPmId(userId);
        projectMapper.insert(project);
        return project;
    }

    public Project getProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(404, "project not found");
        }
        project.setPm(userMapper.selectById(project.getPmId()));
        return project;
    }

    public void updateProject(Long id, UpdateProjectRequest req) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(404, "project not found");
        }
        project.setName(req.getName());
        projectMapper.updateById(project);
    }

    public void deleteProject(Long id) {
        projectMapper.deleteById(id);
    }
}
