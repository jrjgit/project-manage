package com.management.requirement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.requirement.dto.CreateFeatureAssignmentRequest;
import com.management.requirement.entity.Feature;
import com.management.requirement.entity.FeatureAssignment;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.FeatureAssignmentMapper;
import com.management.requirement.mapper.FeatureMapper;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.TaskService;
import com.management.task.dto.CreateTaskRequest;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureAssignmentService {
    private final FeatureAssignmentMapper assignmentMapper;
    private final FeatureMapper featureMapper;
    private final RequirementMapper requirementMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final TaskService taskService;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<FeatureAssignment> listByFeature(Long featureId) {
        List<FeatureAssignment> list = assignmentMapper.selectList(
                new LambdaQueryWrapper<FeatureAssignment>()
                        .eq(FeatureAssignment::getFeatureId, featureId));
        for (FeatureAssignment a : list) {
            if (a.getDeveloperId() != null) a.setDeveloper(userMapper.selectById(a.getDeveloperId()));
        }
        return list;
    }

    @Transactional
    public FeatureAssignment create(CreateFeatureAssignmentRequest req) {
        Feature feature = featureMapper.selectById(req.getFeatureId());
        if (feature == null) throw new BusinessException(404, "功能点不存在");
        Requirement requirement = requirementMapper.selectById(feature.getRequirementId());
        if (requirement == null) throw new BusinessException(404, "需求不存在");

        feature.setStatus("pending_dev");
        featureMapper.updateById(feature);

        if (req.getDeveloperId() != null && userMapper.selectById(req.getDeveloperId()) == null)
            throw new BusinessException(400, "开发人员不存在");

        FeatureAssignment a = new FeatureAssignment();
        a.setFeatureId(req.getFeatureId());
        a.setTerminal(req.getTerminal());
        a.setDeveloperId(req.getDeveloperId());
        a.setStatus("pending");
        assignmentMapper.insert(a);

        // auto-create task
        CreateTaskRequest taskReq = new CreateTaskRequest();
        taskReq.setTitle(requirement.getDescription());
        String taskDesc = req.getDescription();
        if (req.getNotes() != null && !req.getNotes().isBlank()) {
            taskDesc = (taskDesc != null ? taskDesc + "\n" : "") + "备注: " + req.getNotes();
        }
        taskReq.setDescription(taskDesc);
        taskReq.setRequirementId(requirement.getId());
        taskReq.setFeatureId(req.getFeatureId());
        taskReq.setTerminal(req.getTerminal());
        taskReq.setProjectId(requirement.getProjectId());
        taskReq.setDevLeadId(requirement.getDevLeadId());
        taskReq.setAssigneeId(req.getDeveloperId());
        taskReq.setDeadline(req.getDeadline());
        if (req.getPerformance() != null) taskReq.setPerformance(req.getPerformance());

        Task task = taskService.createTask(taskReq);
        a.setAutoTaskId(task.getId());
        assignmentMapper.updateById(a);

        fillAssociations(a);
        log.info("FeatureAssignment created: id={}, featureId={}, terminal={}, developerId={}, autoTaskId={}",
                a.getId(), req.getFeatureId(), req.getTerminal(), req.getDeveloperId(), task.getId());
        return a;
    }

    @Transactional
    public void delete(Long id) {
        FeatureAssignment a = assignmentMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "分配记录不存在");
        if (a.getAutoTaskId() != null) {
            taskMapper.deleteById(a.getAutoTaskId());
        }
        assignmentMapper.deleteById(id);
    }

    private void fillAssociations(FeatureAssignment a) {
        if (a.getDeveloperId() != null) a.setDeveloper(userMapper.selectById(a.getDeveloperId()));
    }
}
