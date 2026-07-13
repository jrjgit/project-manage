package com.management.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.management.bug.entity.Bug;
import com.management.bug.mapper.BugMapper;
import com.management.common.constant.BugStatus;
import com.management.common.constant.TaskStatus;
import com.management.common.constant.UserRole;
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
    private final BugMapper bugMapper;
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

    /**
     * 测试人员受理任务：将当前用户设为 tester_id。
     * 允许 pending_test → testing，或 testing（未指派测试人员）→ 更新 tester_id。
     */
    @Transactional
    public Task acceptTask(Long taskId) {
        JwtUserDetails operator = currentUser();
        if (!UserRole.TESTER.equals(operator.getRole()) && !UserRole.PM.equals(operator.getRole())
                && !UserRole.DEV.equals(operator.getRole()) && !UserRole.DEV_LEAD.equals(operator.getRole())) {
            throw new BusinessException("只有测试人员或项目经理可以受理任务");
        }

        Task task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(404, "任务不存在");
        if (!TaskStatus.PENDING_TEST.equals(task.getStatus())
                && !(TaskStatus.TESTING.equals(task.getStatus()) && task.getTesterId() == null)) {
            throw new BusinessException("只有待测试或未指派测试人员的任务可以受理");
        }

        String oldStatus = task.getStatus();
        boolean statusChanged = !TaskStatus.TESTING.equals(task.getStatus());
        task.setTesterId(operator.getUserId());
        task.setStatus(TaskStatus.TESTING);
        taskMapper.updateById(task);

        TaskStatusHistory history = new TaskStatusHistory();
        history.setTaskId(taskId);
        history.setFromStatus(statusChanged ? oldStatus : "unassigned");
        history.setToStatus(TaskStatus.TESTING);
        history.setChangedBy(operator.getUserId());
        history.setComment("测试人员受理任务");
        historyMapper.insert(history);

        fillAssociations(task);
        log.info("Task {} accepted by tester {} ({})", taskId, operator.getUserId(), operator.getName());
        return task;
    }

    /** 按角色过滤查询任务列表（支持可选分页） */
    public Object listTasks(String projectId, String status, String priority, String requirementId, String iterationId,
                            Integer page, Integer size) {
        JwtUserDetails u = currentUser();
        LambdaQueryWrapper<Task> q = new LambdaQueryWrapper<>();

        // 按需求或迭代查询时跳过角色过滤（查看维度是需求/迭代而非"我的任务"）
        boolean scoped = (requirementId != null && !requirementId.isBlank())
                      || (iterationId != null && !iterationId.isBlank());
        if (!scoped) {
            switch (u.getRole()) {
                case UserRole.PM:
                    break;
                case UserRole.DEV_LEAD:
                    q.exists("SELECT 1 FROM task_assignees WHERE task_assignees.task_id = tasks.id AND task_assignees.user_id = {0}", u.getUserId());
                    break;
                case UserRole.DEV:
                    q.exists("SELECT 1 FROM task_assignees WHERE task_assignees.task_id = tasks.id AND task_assignees.user_id = {0}", u.getUserId());
                    break;
                case UserRole.TESTER:
                    if (!scoped) q.eq(Task::getStatus, TaskStatus.TESTING);
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

        // 分页参数可选，不传则返回全部
        if (page != null && size != null) {
            Page<Task> pg = new Page<>(page, size);
            IPage<Task> result = taskMapper.selectPage(pg, q);
            for (Task t : result.getRecords()) {
                fillAssociations(t);
            }
            return result;
        }

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
        if (UserRole.PM.equals(role) || UserRole.DEV_LEAD.equals(role)) return;
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

    /** 删除任务 */
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException(404, "任务不存在");
        taskAssigneeMapper.delete(new LambdaQueryWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, id));
        historyMapper.delete(new LambdaQueryWrapper<TaskStatusHistory>().eq(TaskStatusHistory::getTaskId, id));
        progressHistoryMapper.delete(new LambdaQueryWrapper<TaskProgressHistory>().eq(TaskProgressHistory::getTaskId, id));
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

    /**
     * 创建任务，校验关联实体存在性，初始化指派人列表，发送通知
     */
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
        task.setStatus(TaskStatus.PENDING);
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
            for (CreateTaskRequest.TaskAssigneeItem item : req.getAssignees()) {
                if (userMapper.selectById(item.getUserId()) == null)
                    throw new BusinessException(400, "指派人不存在: " + item.getUserId());
                TaskAssignee ta = new TaskAssignee();
                ta.setTaskId(task.getId());
                ta.setUserId(item.getUserId());
                ta.setPlatform(item.getPlatform());
                ta.setStatus(TaskStatus.PENDING);
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
                ta.setStatus(TaskStatus.PENDING);
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
            ta.setStatus(TaskStatus.PENDING);
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
            notificationService.emitGenericEvent(msg, operator.getName(), notifyTargets, "task", task.getId());
        }

        log.info("Task created: id={}, title={}", task.getId(), task.getTitle());
        return task;
    }

    /**
     * 更新任务基本信息，处理人员变更通知，记录进度历史
     */
    public Task updateTask(Long id, UpdateTaskRequest req) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException(404, "task not found");

        Long oldDevLeadId = task.getDevLeadId();
        Long oldAssigneeId = task.getAssigneeId();
        Long oldTesterId = task.getTesterId();
        String oldStatus = task.getStatus();

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
        boolean assigneeChanged = false;
        if (req.getAssigneeId() != null) {
            if (userMapper.selectById(req.getAssigneeId()) == null)
                throw new BusinessException(400, "指派人不存在");
            if (!req.getAssigneeId().equals(oldAssigneeId)) {
                task.setAssigneeId(req.getAssigneeId());
                assigneeChanged = true;
            }
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

        // 任务转派时重置状态和进度
        if (assigneeChanged) {
            task.setStatus(TaskStatus.PENDING);
            task.setProgress(0);
        }

        taskMapper.updateById(task);

        // 记录转派历史并同步重置任务指派记录状态
        if (assigneeChanged) {
            TaskStatusHistory history = new TaskStatusHistory();
            history.setTaskId(id);
            history.setFromStatus(oldStatus);
            history.setToStatus(TaskStatus.PENDING);
            history.setChangedBy(currentUser().getUserId());
            history.setComment("任务转派，状态重置为待受理");
            historyMapper.insert(history);

            // 将 task_assignees 中旧指派人记录更新为新指派人
            if (oldAssigneeId != null) {
                taskAssigneeMapper.update(null, new LambdaUpdateWrapper<TaskAssignee>()
                        .eq(TaskAssignee::getTaskId, id)
                        .eq(TaskAssignee::getUserId, oldAssigneeId)
                        .set(TaskAssignee::getUserId, req.getAssigneeId())
                        .set(TaskAssignee::getStatus, TaskStatus.PENDING));
            }
        }

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

    /**
     * 变更任务状态，执行状态机流转校验，记录历史，处理多开发场景和自动跳转
     */
    @Transactional
    public void changeStatus(Long taskId, ChangeTaskStatusRequest req) {
        Long operatorId = currentUser().getUserId();
        String role = currentUser().getRole();
        String newStatus = req.getNewStatus();

        Task task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");

        String oldStatus = task.getStatus();

        // rejected 必须有原因
        if (TaskStatus.REJECTED.equals(newStatus) && (req.getComment() == null || req.getComment().isBlank())) {
            throw new BusinessException("reject reason is required");
        }

        // 多开发场景处理
        if (TaskStatus.DEVELOPING.equals(newStatus) && UserRole.DEV.equals(role) && TaskStatus.DEVELOPING.equals(task.getStatus())) {
            taskAssigneeMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TaskAssignee>()
                    .eq(TaskAssignee::getTaskId, taskId)
                    .eq(TaskAssignee::getUserId, operatorId)
                    .set(TaskAssignee::getStatus, TaskStatus.DEVELOPING));
            return;
        }
        if (TaskStatus.DEVELOPED.equals(newStatus) && UserRole.DEV.equals(role)) {
            List<TaskAssignee> assignees = taskAssigneeMapper.selectList(
                    new LambdaUpdateWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, taskId));
            if (!assignees.isEmpty()) {
                taskAssigneeMapper.update(null, new LambdaUpdateWrapper<TaskAssignee>()
                        .eq(TaskAssignee::getTaskId, taskId)
                        .eq(TaskAssignee::getUserId, operatorId)
                        .set(TaskAssignee::getStatus, TaskStatus.DEVELOPED));
                long pending = taskAssigneeMapper.selectCount(new LambdaUpdateWrapper<TaskAssignee>()
                        .eq(TaskAssignee::getTaskId, taskId).ne(TaskAssignee::getStatus, TaskStatus.DEVELOPED));
                if (pending > 0) return; // 还有人没完成
            }
        }

        // 校验流转合法性
        if (!workflowService.isAllowed(role, oldStatus, newStatus, "task")) {
            throw new BusinessException(
                    String.format("角色 %s 不能将任务从 %s 变更为 %s", role, oldStatus, newStatus));
        }

        // testing → closed 时校验该任务下所有 Bug 必须已关闭
        if (TaskStatus.TESTING.equals(oldStatus) && TaskStatus.CLOSED.equals(newStatus)) {
            long openBugs = bugMapper.selectCount(
                    new LambdaQueryWrapper<Bug>().eq(Bug::getTaskId, taskId).ne(Bug::getStatus, BugStatus.CLOSED));
            if (openBugs > 0) {
                throw new BusinessException(
                        "该任务下存在 " + openBugs + " 个未关闭的 Bug，请先处理完再关闭任务");
            }
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

        // 开发完成（developing -> testing）时自动将进度设为100%
        if (TaskStatus.DEVELOPING.equals(oldStatus) && TaskStatus.TESTING.equals(newStatus)) {
            task.setProgress(100);
            taskMapper.updateById(task);
            TaskProgressHistory ph = new TaskProgressHistory();
            ph.setTaskId(taskId);
            ph.setProgress(100);
            ph.setComment("系统自动将进度设为100%（开发完成）");
            ph.setCreatedBy(operatorId);
            progressHistoryMapper.insert(ph);
        }

        log.info("Task {} status changed: {} -> {} by user {} ({})",
                taskId, oldStatus, newStatus, operatorId, role);

        // 通知目标
        String operatorName = currentUser().getName();
        List<User> targets = resolveTaskNotifyTargets(task, oldStatus, newStatus);
        notificationService.emitTaskEvent(task, oldStatus, newStatus, operatorName, targets, req.getComment());

        // 关键：developed -> pending_test 自动跳转
        if (TaskStatus.DEVELOPED.equals(newStatus)) {
            task.setStatus(TaskStatus.PENDING_TEST);
            taskMapper.updateById(task);

            TaskStatusHistory autoHistory = new TaskStatusHistory();
            autoHistory.setTaskId(taskId);
            autoHistory.setFromStatus(TaskStatus.DEVELOPED);
            autoHistory.setToStatus(TaskStatus.PENDING_TEST);
            autoHistory.setChangedBy(operatorId);
            autoHistory.setComment("系统自动将任务加入测试池");
            historyMapper.insert(autoHistory);

        }

        // rejected 时保存原因到任务
        if (TaskStatus.REJECTED.equals(newStatus)) {
            taskMapper.update(null, new LambdaUpdateWrapper<Task>()
                    .eq(Task::getId, taskId).set(Task::getRejectReason, req.getComment()));
        }

        // dev 开始开发时更新 TaskAssignee
        if (TaskStatus.DEVELOPING.equals(newStatus) && UserRole.DEV.equals(role)) {
            taskAssigneeMapper.update(null, new LambdaUpdateWrapper<TaskAssignee>()
                    .eq(TaskAssignee::getTaskId, taskId)
                    .eq(TaskAssignee::getUserId, operatorId)
                    .set(TaskAssignee::getStatus, TaskStatus.DEVELOPING));
        }
    }

    /** 根据状态流转确定通知目标（通知目标解析） */
    private List<User> resolveTaskNotifyTargets(Task task, String oldStatus, String newStatus) {
        List<User> targets = new ArrayList<>();
        String key = oldStatus + "->" + newStatus;
        switch (key) {
            case "pending->developing":
                if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
                break;
            case "developing->testing":
                if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
                if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
                break;
            case "testing->closed":
                if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
                if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
                break;
            case "testing->developing":
                if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
                if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
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
        ta.setStatus(TaskStatus.PENDING);
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
