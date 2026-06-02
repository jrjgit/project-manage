package com.management.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.common.notification.NotificationService;
import com.management.common.workflow.WorkflowService;
import com.management.project.entity.Project;
import com.management.project.mapper.ProjectMapper;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.dto.*;
import com.management.task.entity.*;
import com.management.task.mapper.*;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskAssigneeMapper taskAssigneeMapper;
    private final TaskStatusHistoryMapper historyMapper;
    private final TaskProgressHistoryMapper progressHistoryMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;
    private final RequirementMapper requirementMapper;
    /** 获取当前用户 */
    public JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 按角色过滤查询任务列表 */
    public List<Task> listTasks(String projectId, String status, String priority, String requirementId, String iterationId) {
        JwtUserDetails u = currentUser();
        LambdaQueryWrapper<Task> q = new LambdaQueryWrapper<>();

        // 按需求或迭代查询时跳过角色过滤（查看维度是需求/迭代而非"我的任务"）
        boolean scoped = (requirementId != null && !requirementId.isBlank())
                      || (iterationId != null && !iterationId.isBlank());
        if (!scoped) {
            switch (u.getRole()) {
                case "pm":
                    break;
                case "dev_lead":
                    q.inSql(Task::getId,
                            "SELECT task_id FROM task_assignees WHERE user_id = " + u.getUserId());
                    break;
                case "dev":
                    q.inSql(Task::getId,
                            "SELECT task_id FROM task_assignees WHERE user_id = " + u.getUserId());
                    break;
                case "tester_lead":
                    q.and(w -> w.eq(Task::getTesterLeadId, u.getUserId())
                            .or().in(Task::getStatus, List.of("pending_test", "testing", "passed", "rejected")));
                    break;
                case "tester":
                    q.and(w -> w.eq(Task::getTesterId, u.getUserId())
                            .or().eq(Task::getCreatorId, u.getUserId()));
                    break;
                default:
                    q.apply("1=0");
            }
        }

        if (projectId != null && !projectId.isBlank()) q.eq(Task::getProjectId, projectId);
        if (status != null && !status.isBlank()) q.eq(Task::getStatus, status);
        if (priority != null && !priority.isBlank()) q.eq(Task::getPriority, priority);
        if (requirementId != null && !requirementId.isBlank()) q.eq(Task::getRequirementId, requirementId);
        if (iterationId != null && !iterationId.isBlank()) q.eq(Task::getIterationId, iterationId);
        applyProjectScopeFilter(u, q);

        q.orderByDesc(Task::getUpdatedAt);
        List<Task> tasks = taskMapper.selectList(q);
        for (Task t : tasks) {
            fillAssociations(t);
        }
        return tasks;
    }

    /** 获取任务详情（含 assignees） */
    public TaskDetail getTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException(404, "task not found");
        fillAssociations(task);

        List<TaskAssignee> assignees = taskAssigneeMapper.selectList(
                new LambdaUpdateWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, id));
        for (TaskAssignee ta : assignees) {
            ta.setUser(userMapper.selectById(ta.getUserId()));
        }

        TaskDetail detail = new TaskDetail();
        detail.setTask(task);
        detail.setAssignees(assignees);
        return detail;
    }

    /** 获取状态历史 */
    public List<TaskStatusHistory> getTaskHistory(Long id) {
        List<TaskStatusHistory> list = historyMapper.selectList(
                new LambdaQueryWrapper<TaskStatusHistory>()
                        .eq(TaskStatusHistory::getTaskId, id)
                        .orderByDesc(TaskStatusHistory::getChangedAt));
        for (TaskStatusHistory h : list) {
            h.setUser(userMapper.selectById(h.getChangedBy()));
        }
        return list;
    }

    /** 获取任务进度上报历史 */
    public List<TaskProgressHistory> getProgressHistory(Long id) {
        List<TaskProgressHistory> list = progressHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskProgressHistory>()
                        .eq(TaskProgressHistory::getTaskId, id)
                        .orderByDesc(TaskProgressHistory::getCreatedAt));
        for (TaskProgressHistory h : list) {
            if (h.getCreatedBy() != null) h.setUser(userMapper.selectById(h.getCreatedBy()));
        }
        return list;
    }

    private void applyProjectScopeFilter(JwtUserDetails u, LambdaQueryWrapper<Task> q) {
        String role = u.getRole();
        if ("pm".equals(role) || "dev_lead".equals(role) || "tester_lead".equals(role)) return;
        List<Long> projectIds = getVisibleProjectIds(u.getUserId());
        if (projectIds.isEmpty()) {
            q.and(w -> w.isNull(Task::getProjectId));
        } else {
            q.and(w -> w.in(Task::getProjectId, projectIds).or().isNull(Task::getProjectId));
        }
    }

    private List<Long> getVisibleProjectIds(Long userId) {
        List<Project> all = projectMapper.selectList(null);
        return all.stream()
                .filter(p -> p.getHrScope() != null && !p.getHrScope().isBlank())
                .filter(p -> Arrays.stream(p.getHrScope().split(","))
                        .map(String::trim).anyMatch(id -> id.equals(String.valueOf(userId))))
                .map(Project::getId)
                .collect(Collectors.toList());
    }

    /** 填充关联对象 */
    /** 删除任务 */
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException(404, "任务不存在");
        taskAssigneeMapper.delete(new LambdaQueryWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, id));
        taskMapper.deleteById(id);
    }

    private void fillAssociations(Task t) {
        if (t.getProjectId() != null) {
            Project p = projectMapper.selectById(t.getProjectId());
            if (p != null) { p.setPm(userMapper.selectById(p.getPmId())); t.setProject(p); }
        }
        if (t.getCreatorId() != null) t.setCreator(userMapper.selectById(t.getCreatorId()));
        if (t.getAssigneeId() != null) t.setAssignee(userMapper.selectById(t.getAssigneeId()));
        if (t.getDevLeadId() != null) t.setDevLead(userMapper.selectById(t.getDevLeadId()));
        if (t.getTesterLeadId() != null) t.setTesterLead(userMapper.selectById(t.getTesterLeadId()));
        if (t.getTesterId() != null) t.setTester(userMapper.selectById(t.getTesterId()));
        if (t.getRequirementId() != null) {
            Requirement req = requirementMapper.selectById(t.getRequirementId());
            if (req != null) { t.setRequirementName(req.getNumber()); t.setRequirementDesc(req.getDescription()); }
        }
    }

    /** Task 详情 DTO */
    @lombok.Data
    public static class TaskDetail {
        private Task task;
        private List<TaskAssignee> assignees;
    }

    /** 创建任务（仅 PM） */
    @Transactional
    public Task createTask(CreateTaskRequest req) {
        Long userId = currentUser().getUserId();

        LocalDateTime deadline = null;
        if (req.getDeadline() != null && !req.getDeadline().isBlank()) {
            String d = req.getDeadline().trim();
            if (d.contains("T")) {
                deadline = java.time.OffsetDateTime.parse(d).toLocalDateTime();
            } else {
                deadline = LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            }
        }

        if (req.getProjectId() != null && projectMapper.selectById(req.getProjectId()) == null)
            throw new BusinessException(400, "项目不存在");
        if (req.getDevLeadId() != null && userMapper.selectById(req.getDevLeadId()) == null)
            throw new BusinessException(400, "开发组长不存在");
        if (req.getAssigneeId() != null && userMapper.selectById(req.getAssigneeId()) == null)
            throw new BusinessException(400, "指派人不存在");
        if (req.getTesterId() != null && userMapper.selectById(req.getTesterId()) == null)
            throw new BusinessException(400, "测试人员不存在");
        if (req.getRequirementId() != null && requirementMapper.selectById(req.getRequirementId()) == null)
            throw new BusinessException(400, "关联需求不存在");
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setStatus("pending");
        task.setPriority(req.getPriority() != null ? req.getPriority() : "medium");
        task.setProjectId(req.getProjectId());
        task.setCreatorId(userId);
        task.setDevLeadId(req.getDevLeadId());
        task.setDeadline(deadline);
        if (req.getPerformance() != null) task.setPerformance(req.getPerformance());
        if (req.getAssigneeId() != null) task.setAssigneeId(req.getAssigneeId());
        if (req.getTesterId() != null) task.setTesterId(req.getTesterId());
        if (req.getRequirementId() != null) task.setRequirementId(req.getRequirementId());
        if (req.getTerminal() != null) task.setTerminal(req.getTerminal());
        if (req.getIterationId() != null) task.setIterationId(req.getIterationId());
        if (req.getTestPerformance() != null) task.setTestPerformance(req.getTestPerformance());
        taskMapper.insert(task);

        // 处理多指派人
        if (req.getAssignees() != null && !req.getAssignees().isEmpty()) {
            for (var item : req.getAssignees()) {
                if (userMapper.selectById(item.getUserId()) == null)
                    throw new BusinessException(400, "指派人不存在: " + item.getUserId());
                TaskAssignee ta = new TaskAssignee();
                ta.setTaskId(task.getId());
                ta.setUserId(item.getUserId());
                ta.setPlatform(item.getPlatform());
                ta.setStatus("pending");
                taskAssigneeMapper.insert(ta);
            }
            if (task.getAssigneeId() == null) {
                task.setAssigneeId(req.getAssignees().get(0).getUserId());
                taskMapper.updateById(task);
            }
        } else if (req.getAssigneeIds() != null && !req.getAssigneeIds().isEmpty()) {
            for (Long uid : req.getAssigneeIds()) {
                if (userMapper.selectById(uid) == null)
                    throw new BusinessException(400, "指派人不存在: " + uid);
                TaskAssignee ta = new TaskAssignee();
                ta.setTaskId(task.getId());
                ta.setUserId(uid);
                ta.setStatus("pending");
                taskAssigneeMapper.insert(ta);
            }
            if (task.getAssigneeId() == null) {
                task.setAssigneeId(req.getAssigneeIds().get(0));
                taskMapper.updateById(task);
            }
        } else if (req.getAssigneeId() != null) {
            TaskAssignee ta = new TaskAssignee();
            ta.setTaskId(task.getId());
            ta.setUserId(req.getAssigneeId());
            ta.setStatus("pending");
            taskAssigneeMapper.insert(ta);
        }

        fillAssociations(task);

        // 通知相关人员
        JwtUserDetails operator = currentUser();
        List<User> notifyTargets = new ArrayList<>();
        if (task.getDevLeadId() != null) addUser(notifyTargets, task.getDevLeadId());
        if (task.getAssigneeId() != null) addUser(notifyTargets, task.getAssigneeId());
        if (task.getTesterId() != null) addUser(notifyTargets, task.getTesterId());
        if (!notifyTargets.isEmpty()) {
            String msg = "任务【" + task.getTitle() + "】已创建，操作人：" + operator.getName() + "。请及时处理。";
            notificationService.emitGenericEvent(msg, operator.getName(), notifyTargets);
        }

        log.info("Task created: id={}, title={}", task.getId(), task.getTitle());
        return task;
    }

    /** 更新任务基本信息 */
    public Task updateTask(Long id, UpdateTaskRequest req) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException(404, "task not found");

        Long oldDevLeadId = task.getDevLeadId();
        Long oldAssigneeId = task.getAssigneeId();
        Long oldTesterLeadId = task.getTesterLeadId();
        Long oldTesterId = task.getTesterId();

        if (req.getTitle() != null && !req.getTitle().isBlank()) task.setTitle(req.getTitle());
        if (req.getDescription() != null) task.setDescription(req.getDescription());
        if (req.getPriority() != null && !req.getPriority().isBlank()) task.setPriority(req.getPriority());
        if (req.getProjectId() != null) {
            if (projectMapper.selectById(req.getProjectId()) == null)
                throw new BusinessException(400, "项目不存在");
            task.setProjectId(req.getProjectId());
        }
        if (req.getDevLeadId() != null) {
            if (userMapper.selectById(req.getDevLeadId()) == null)
                throw new BusinessException(400, "开发组长不存在");
            task.setDevLeadId(req.getDevLeadId());
        }
        if (req.getAssigneeId() != null) {
            if (userMapper.selectById(req.getAssigneeId()) == null)
                throw new BusinessException(400, "指派人不存在");
            task.setAssigneeId(req.getAssigneeId());
        }
        if (req.getTesterLeadId() != null) {
            if (userMapper.selectById(req.getTesterLeadId()) == null)
                throw new BusinessException(400, "测试组长不存在");
            task.setTesterLeadId(req.getTesterLeadId());
        }
        if (req.getTesterId() != null) {
            if (userMapper.selectById(req.getTesterId()) == null)
                throw new BusinessException(400, "测试人员不存在");
            task.setTesterId(req.getTesterId());
        }
        if (req.getRequirementId() != null) {
            if (requirementMapper.selectById(req.getRequirementId()) == null)
                throw new BusinessException(400, "关联需求不存在");
            task.setRequirementId(req.getRequirementId());
        }
        if (req.getDeadline() != null && !req.getDeadline().isBlank()) {
            String d = req.getDeadline().trim();
            if (d.contains("T")) {
                task.setDeadline(java.time.OffsetDateTime.parse(d).toLocalDateTime());
            } else {
                task.setDeadline(LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
            }
        }
        if (req.getPerformance() != null) task.setPerformance(req.getPerformance());
        if (req.getTestPerformance() != null) task.setTestPerformance(req.getTestPerformance());
        if (req.getTerminal() != null) task.setTerminal(req.getTerminal());
        if (req.getProgress() != null) {
            Integer oldProgress = task.getProgress();
            task.setProgress(req.getProgress());
            if (!req.getProgress().equals(oldProgress)) {
                TaskProgressHistory ph = new TaskProgressHistory();
                ph.setTaskId(id);
                ph.setProgress(req.getProgress());
                ph.setComment(req.getDescription());
                ph.setCreatedBy(currentUser().getUserId());
                progressHistoryMapper.insert(ph);
            }
        }
        taskMapper.updateById(task);

        if (req.getIterationId() != null) {
            if (req.getIterationId().isBlank()) {
                taskMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Task>()
                        .eq(Task::getId, id).set(Task::getIterationId, null));
            } else {
                task.setIterationId(req.getIterationId());
                taskMapper.updateById(task);
            }
        }

        JwtUserDetails op = currentUser();
        String title = task.getTitle();

        // dev_lead_id 变化时通知新开发组长
        if (req.getDevLeadId() != null && (oldDevLeadId == null || !req.getDevLeadId().equals(oldDevLeadId))) {
            User newDevLead = userMapper.selectById(req.getDevLeadId());
            if (newDevLead != null) {
                notificationService.emitGenericEvent(
                        "任务【" + title + "】您被指派为开发组长，操作人：" + op.getName(),
                        op.getName(), List.of(newDevLead));
            }
        }

        // assignee_id 变化时通知新指派人
        if (req.getAssigneeId() != null && (oldAssigneeId == null || !req.getAssigneeId().equals(oldAssigneeId))) {
            User newAssignee = userMapper.selectById(req.getAssigneeId());
            if (newAssignee != null) {
                notificationService.emitGenericEvent(
                        "任务【" + title + "】您被指派为开发人员，操作人：" + op.getName(),
                        op.getName(), List.of(newAssignee));
            }
        }

        // tester_lead_id 变化时通知新测试组长
        if (req.getTesterLeadId() != null && (oldTesterLeadId == null || !req.getTesterLeadId().equals(oldTesterLeadId))) {
            User newTesterLead = userMapper.selectById(req.getTesterLeadId());
            if (newTesterLead != null) {
                notificationService.emitGenericEvent(
                        "任务【" + title + "】您被指派为测试组长，操作人：" + op.getName(),
                        op.getName(), List.of(newTesterLead));
            }
        }

        // tester_id 变化时通知新测试人员
        if (req.getTesterId() != null && (oldTesterId == null || !req.getTesterId().equals(oldTesterId))) {
            User newTester = userMapper.selectById(req.getTesterId());
            if (newTester != null) {
                notificationService.emitGenericEvent(
                        "任务【" + title + "】您被指派为测试人员，操作人：" + op.getName(),
                        op.getName(), List.of(newTester));
            }
        }

        fillAssociations(task);
        return task;
    }

    /** 变更任务状态（核心状态机逻辑） */
    @Transactional
    public void changeStatus(Long taskId, ChangeTaskStatusRequest req) {
        Long operatorId = currentUser().getUserId();
        String role = currentUser().getRole();
        String newStatus = req.getNewStatus();

        Task task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");

        String oldStatus = task.getStatus();

        // rejected 必须有原因
        if ("rejected".equals(newStatus) && (req.getComment() == null || req.getComment().isBlank())) {
            throw new BusinessException("reject reason is required");
        }

        // 多开发场景处理
        if ("developing".equals(newStatus) && "dev".equals(role) && "developing".equals(task.getStatus())) {
            taskAssigneeMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TaskAssignee>()
                    .eq(TaskAssignee::getTaskId, taskId)
                    .eq(TaskAssignee::getUserId, operatorId)
                    .set(TaskAssignee::getStatus, "developing"));
            return;
        }
        if ("developed".equals(newStatus) && "dev".equals(role)) {
            List<TaskAssignee> assignees = taskAssigneeMapper.selectList(
                    new LambdaUpdateWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, taskId));
            if (!assignees.isEmpty()) {
                taskAssigneeMapper.update(null, new LambdaUpdateWrapper<TaskAssignee>()
                        .eq(TaskAssignee::getTaskId, taskId)
                        .eq(TaskAssignee::getUserId, operatorId)
                        .set(TaskAssignee::getStatus, "developed"));
                long pending = taskAssigneeMapper.selectCount(new LambdaUpdateWrapper<TaskAssignee>()
                        .eq(TaskAssignee::getTaskId, taskId).ne(TaskAssignee::getStatus, "developed"));
                if (pending > 0) return; // 还有人没完成
            }
        }

        // 校验流转合法性
        if (!workflowService.isAllowed(role, oldStatus, newStatus, "task")) {
            throw new BusinessException(
                    String.format("角色 %s 不能将任务从 %s 变更为 %s", role, oldStatus, newStatus));
        }

        // 更新状态
        task.setStatus(newStatus);
        taskMapper.updateById(task);

        // 记录历史
        TaskStatusHistory history = new TaskStatusHistory();
        history.setTaskId(taskId);
        history.setFromStatus(oldStatus);
        history.setToStatus(newStatus);
        history.setChangedBy(operatorId);
        history.setComment(req.getComment());
        historyMapper.insert(history);

        log.info("Task {} status changed: {} -> {} by user {} ({})",
                taskId, oldStatus, newStatus, operatorId, role);

        // 通知目标
        String operatorName = currentUser().getName();
        List<User> targets = resolveTaskNotifyTargets(task, oldStatus, newStatus);
        notificationService.emitTaskEvent(task, oldStatus, newStatus, operatorName, targets, req.getComment());

        // 关键：developed -> pending_test 自动跳转
        if ("developed".equals(newStatus)) {
            task.setStatus("pending_test");
            taskMapper.updateById(task);

            TaskStatusHistory autoHistory = new TaskStatusHistory();
            autoHistory.setTaskId(taskId);
            autoHistory.setFromStatus("developed");
            autoHistory.setToStatus("pending_test");
            autoHistory.setChangedBy(operatorId);
            autoHistory.setComment("系统自动将任务加入测试池");
            historyMapper.insert(autoHistory);

            if (task.getTesterLeadId() != null) {
                User testerLead = userMapper.selectById(task.getTesterLeadId());
                if (testerLead != null) {
                    notificationService.emitTaskEvent(task, "developed", "pending_test",
                            null, List.of(testerLead), "任务已加入测试池");
                }
            }
        }

        // rejected 时保存原因到任务
        if ("rejected".equals(newStatus)) {
            taskMapper.update(null, new LambdaUpdateWrapper<Task>()
                    .eq(Task::getId, taskId).set(Task::getRejectReason, req.getComment()));
        }

        // dev 开始开发时更新 TaskAssignee
        if ("developing".equals(newStatus) && "dev".equals(role)) {
            taskAssigneeMapper.update(null, new LambdaUpdateWrapper<TaskAssignee>()
                    .eq(TaskAssignee::getTaskId, taskId)
                    .eq(TaskAssignee::getUserId, operatorId)
                    .set(TaskAssignee::getStatus, "developing"));
        }
    }

    /** 根据状态流转确定通知目标（对等 Go resolveTaskNotifyTargets） */
    private List<User> resolveTaskNotifyTargets(Task task, String oldStatus, String newStatus) {
        List<User> targets = new ArrayList<>();
        String key = oldStatus + "->" + newStatus;

        switch (key) {
            case "pending->assigned_lead":
                if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
                break;
            case "assigned_lead->developing":
                if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
                break;
            case "developing->developed":
                if (task.getTesterLeadId() != null) addUser(targets, task.getTesterLeadId());
                break;
            case "pending_test->testing":
                if (task.getTesterLeadId() != null) addUser(targets, task.getTesterLeadId());
                if (task.getTesterId() != null) addUser(targets, task.getTesterId());
                break;
            case "testing->passed":
                Project p = projectMapper.selectById(task.getProjectId());
                if (p != null) addUser(targets, p.getPmId());
                if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
                if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
                break;
            case "testing->rejected":
                if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
                if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
                break;
            case "rejected->developing":
                if (task.getTesterId() != null) addUser(targets, task.getTesterId());
                if (task.getTesterLeadId() != null) addUser(targets, task.getTesterLeadId());
                break;
        }
        return targets;
    }

    private void addUser(List<User> targets, Long userId) {
        User u = userMapper.selectById(userId);
        if (u != null) targets.add(u);
    }

    /** 添加指派人 */
    public TaskAssignee addAssignee(Long taskId, AddTaskAssigneeRequest req) {
        TaskAssignee existing = taskAssigneeMapper.selectOne(new LambdaUpdateWrapper<TaskAssignee>()
                .eq(TaskAssignee::getTaskId, taskId).eq(TaskAssignee::getUserId, req.getUserId()));
        if (existing != null) throw new BusinessException("user already assigned to this task");

        TaskAssignee ta = new TaskAssignee();
        ta.setTaskId(taskId);
        ta.setUserId(req.getUserId());
        ta.setPlatform(req.getPlatform());
        ta.setStatus("pending");
        taskAssigneeMapper.insert(ta);

        Task task = taskMapper.selectById(taskId);
        if (task != null && task.getAssigneeId() == null) {
            task.setAssigneeId(req.getUserId());
            taskMapper.updateById(task);
        }
        return ta;
    }

    /** 移除指派人 */
    public void removeAssignee(Long taskId, Long userId) {
        taskAssigneeMapper.delete(new LambdaUpdateWrapper<TaskAssignee>()
                .eq(TaskAssignee::getTaskId, taskId).eq(TaskAssignee::getUserId, userId));
    }
}
