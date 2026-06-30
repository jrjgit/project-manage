package com.management.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.bug.entity.Bug;
import com.management.bug.mapper.BugMapper;
import com.management.common.jwt.JwtUserDetails;
import com.management.iteration.entity.Iteration;
import com.management.iteration.mapper.IterationMapper;
import com.management.project.entity.Project;
import com.management.project.mapper.ProjectMapper;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.entity.Task;
import com.management.task.entity.TaskProgressHistory;
import com.management.task.mapper.TaskMapper;
import com.management.task.mapper.TaskProgressHistoryMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TaskMapper taskMapper;
    private final BugMapper bugMapper;
    private final RequirementMapper requirementMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final IterationMapper iterationMapper;
    private final TaskProgressHistoryMapper taskProgressHistoryMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public Map<String, Object> getDashboard() {
        JwtUserDetails u = currentUser();
        return switch (u.getRole()) {
            case "pm" -> pmDashboard(u);
            case "dev_lead" -> devLeadDashboard(u);
            case "dev" -> devDashboard(u);
            case "tester" -> testerDashboard(u);
            default -> Map.of("message", "unknown role");
        };
    }

    private Map<String, Object> pmDashboard(JwtUserDetails u) {
        List<Requirement> allReqs = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>().orderByDesc(Requirement::getCreatedAt));
        List<Task> allTasks = taskMapper.selectList(null);

        long pendingTaskReqs = allReqs.stream().filter(r -> "pending_task".equals(r.getStatus())).count();
        long inProgressReqs = allReqs.stream().filter(r -> "in_progress".equals(r.getStatus())).count();
        long testingReqs = allReqs.stream().filter(r ->
                "integration_test".equals(r.getStatus()) || "business_test".equals(r.getStatus())).count();
        long pendingReleaseReqs = allReqs.stream().filter(r -> "pending_release".equals(r.getStatus())).count();

        long overdueTasks = allTasks.stream().filter(t ->
                !"closed".equals(t.getStatus()) && t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now())).count();

        List<Map<String, Object>> overdueList = allTasks.stream()
                .filter(t -> !"closed".equals(t.getStatus()) && t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Task::getDeadline))
                .limit(8).map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("title", t.getTitle());
                    fillTaskUser(t);
                    m.put("assignee", t.getAssignee() != null ? t.getAssignee().getName() : "-");
                    m.put("deadline", t.getDeadline() != null ? t.getDeadline().toString() : null);
                    m.put("overdueDays", t.getDeadline() != null
                            ? LocalDateTime.now().toLocalDate().toEpochDay() - t.getDeadline().toLocalDate().toEpochDay() : 0);
                    return m;
                }).collect(Collectors.toList());

        // Pipeline
        long pipePlanned = pendingTaskReqs;
        long pipeInProgress = inProgressReqs;
        long pipeTesting = testingReqs;
        long pipePendingRelease = pendingReleaseReqs;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", Map.of(
                "totalRequirements", (long) allReqs.size(),
                "pendingTask", pendingTaskReqs,
                "inProgress", inProgressReqs,
                "testing", testingReqs,
                "pendingRelease", pendingReleaseReqs,
                "overdueTasks", overdueTasks
        ));
        result.put("overdueTasks", overdueList);
        result.put("pipeline", Map.of("pendingTask", pipePlanned, "inProgress", pipeInProgress,
                "testing", pipeTesting, "pendingRelease", pipePendingRelease));
        return result;
    }

    private Map<String, Object> devLeadDashboard(JwtUserDetails u) {
        List<Task> teamTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getDevLeadId, u.getUserId()));
        long total = teamTasks.size();
        long developing = teamTasks.stream().filter(t -> "developing".equals(t.getStatus())).count();
        long done = teamTasks.stream().filter(t -> "closed".equals(t.getStatus())).count();
        long overdue = teamTasks.stream().filter(t ->
                !"closed".equals(t.getStatus()) && t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now())).count();

        List<Map<String, Object>> overdueList = teamTasks.stream()
                .filter(t -> !"closed".equals(t.getStatus()) && t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Task::getDeadline))
                .limit(8).map(t -> taskToMap(t)).collect(Collectors.toList());

        List<Map<String, Object>> board = List.of(
                boardCol(teamTasks, "pending", "待受理"),
                boardCol(teamTasks, "developing", "开发中"),
                boardCol(teamTasks, "testing", "综合测试中"),
                boardCol(teamTasks, "closed", "已完成")
        );

        // Per-person workload
        Map<Long, List<Task>> byPerson = teamTasks.stream()
                .filter(t -> t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(Task::getAssigneeId));
        List<Map<String, Object>> workload = byPerson.entrySet().stream().map(e -> {
            User person = userMapper.selectById(e.getKey());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", person != null ? person.getName() : "未知");
            m.put("total", e.getValue().size());
            m.put("developing", e.getValue().stream().filter(t -> "developing".equals(t.getStatus())).count());
            m.put("done", e.getValue().stream().filter(t -> "closed".equals(t.getStatus())).count());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", Map.of("total", total, "developing", developing, "done", done, "overdue", overdue));
        result.put("overdueTasks", overdueList);
        result.put("board", board);
        result.put("workload", workload);
        return result;
    }

    private Map<String, Object> devDashboard(JwtUserDetails u) {
        List<Task> myTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getAssigneeId, u.getUserId()));
        long pending = myTasks.stream().filter(t -> "pending".equals(t.getStatus())).count();
        long developing = myTasks.stream().filter(t -> "developing".equals(t.getStatus())).count();
        long done = myTasks.stream().filter(t -> "closed".equals(t.getStatus())).count();
        long overdue = myTasks.stream().filter(t ->
                !"closed".equals(t.getStatus()) && t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now())).count();

        List<Map<String, Object>> taskList = myTasks.stream()
                .sorted(Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(t -> {
                    Map<String, Object> m = taskToMap(t);
                    m.put("overdue", t.getDeadline() != null && !"closed".equals(t.getStatus())
                            && t.getDeadline().isBefore(LocalDateTime.now()));
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", Map.of("total", myTasks.size(), "pending", pending, "developing", developing, "done", done, "overdue", overdue));
        result.put("tasks", taskList);
        return result;
    }

    private Map<String, Object> testerDashboard(JwtUserDetails u) {
        long pendingTest = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, "testing").eq(Task::getTesterId, u.getUserId()));
        long testing = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, "testing"));

        List<Bug> myBugs = bugMapper.selectList(
                new LambdaQueryWrapper<Bug>().eq(Bug::getCreatorId, u.getUserId()).or().eq(Bug::getAssigneeId, u.getUserId()));
        long bugPendingVerify = myBugs.stream().filter(b -> "pending_verify".equals(b.getStatus())).count();
        long bugTotal = myBugs.size();

        List<Map<String, Object>> testingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, "testing")
                        .and(w -> w.eq(Task::getTesterId, u.getUserId()).or().isNull(Task::getTesterId)))
                .stream().limit(10).map(t -> taskToMap(t)).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", Map.of("pendingTest", pendingTest, "testing", testing, "bugPendingVerify", bugPendingVerify, "bugTotal", bugTotal));
        result.put("testingTasks", testingTasks);
        result.put("bugs", myBugs.stream().limit(10).map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("title", b.getTitle());
            m.put("status", b.getStatus());
            m.put("creatorId", b.getCreatorId());
            return m;
        }).collect(Collectors.toList()));
        return result;
    }

    /** 开发者工作台 — 看板聚合接口 */
    public Map<String, Object> developerDashboard() {
        JwtUserDetails u = currentUser();
        Long userId = u.getUserId();

        List<Task> allTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getAssigneeId, userId)
                        .or()
                        .inSql(Task::getId,
                                "SELECT task_id FROM task_assignees WHERE user_id = " + userId));
        for (Task t : allTasks) fillTaskUser(t);

        List<Task> pendingTasks = allTasks.stream().filter(t -> "pending".equals(t.getStatus())).collect(Collectors.toList());
        List<Task> developingTasks = allTasks.stream().filter(t -> "developing".equals(t.getStatus())).collect(Collectors.toList());
        List<Task> testingTasks = allTasks.stream().filter(t -> "testing".equals(t.getStatus())).collect(Collectors.toList());

        long total = allTasks.size();
        long developing = developingTasks.size();
        long done = allTasks.stream().filter(t -> "closed".equals(t.getStatus())).count();
        long overdue = allTasks.stream().filter(t -> !"closed".equals(t.getStatus())
                && t.getDeadline() != null && t.getDeadline().isBefore(LocalDateTime.now())).count();

        List<Bug> bugs = bugMapper.selectList(
                new LambdaQueryWrapper<Bug>()
                        .eq(Bug::getAssigneeId, userId)
                        .in(Bug::getStatus, "unfixed")
                        .orderByDesc(Bug::getUpdatedAt));
        for (Bug b : bugs) {
            if (b.getTaskId() != null) b.setTask(taskMapper.selectById(b.getTaskId()));
        }

        // 批量加载 Bug 相关人员
        java.util.Set<Long> bugUserIds = new java.util.HashSet<>();
        for (Bug b : bugs) {
            if (b.getCreatorId() != null) bugUserIds.add(b.getCreatorId());
            if (b.getAssigneeId() != null) bugUserIds.add(b.getAssigneeId());
        }
        java.util.Map<Long, User> bugUserMap = new java.util.HashMap<>();
        if (!bugUserIds.isEmpty()) {
            for (Long uid : bugUserIds) {
                User user = userMapper.selectById(uid);
                if (user != null) bugUserMap.put(uid, user);
            }
        }

        // 缓存 Bug 关联任务的需求信息，用于前端按需求编号筛选
        java.util.Map<Long, Requirement> bugReqCache = new java.util.HashMap<>();

        List<Map<String, Object>> board = List.of(
                Map.of("status", "pending", "label", "待受理", "tasks", pendingTasks.stream().map(this::taskToMap).collect(Collectors.toList())),
                Map.of("status", "developing", "label", "开发中", "tasks", developingTasks.stream().map(this::taskToMap).collect(Collectors.toList())),
                Map.of("status", "testing", "label", "综合测试中", "tasks", testingTasks.stream().map(this::taskToMap).collect(Collectors.toList()))
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", Map.of("total", total, "developing", developing, "done", done, "overdue", overdue, "pendingBugs", (long) bugs.size()));
        result.put("board", board);
        result.put("bugs", bugs.stream().map(b -> bugToMap(b, bugUserMap, bugReqCache)).collect(Collectors.toList()));
        return result;
    }

    /** 测试工作台 — 看板聚合接口 */
    public Map<String, Object> testerDashboardV2() {
        JwtUserDetails u = currentUser();
        Long userId = u.getUserId();

        List<Task> allTestingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().in(Task::getStatus, "pending_test", "testing"));
        for (Task t : allTestingTasks) fillTaskUser(t);

        // 批量加载待测试任务的 Bug 创建人
        List<Long> testingTaskIds = allTestingTasks.stream().map(Task::getId).collect(Collectors.toList());
        java.util.Map<Long, java.util.List<String>> bugCreatorsByTask = new java.util.HashMap<>();
        if (!testingTaskIds.isEmpty()) {
            List<Bug> taskBugs = bugMapper.selectList(
                    new LambdaQueryWrapper<Bug>().in(Bug::getTaskId, testingTaskIds));
            java.util.Map<Long, java.util.Set<String>> creatorsSet = new java.util.HashMap<>();
            for (Bug b : taskBugs) {
                if (b.getCreatorId() != null && b.getTaskId() != null) {
                    User creator = userMapper.selectById(b.getCreatorId());
                    if (creator != null) {
                        creatorsSet.computeIfAbsent(b.getTaskId(), k -> new java.util.LinkedHashSet<>()).add(creator.getName());
                    }
                }
            }
            for (java.util.Map.Entry<Long, java.util.Set<String>> entry : creatorsSet.entrySet()) {
                bugCreatorsByTask.put(entry.getKey(), new java.util.ArrayList<>(entry.getValue()));
            }
        }

        // 待验证Bug：当前用户创建的非unfixed/closed的Bug
        List<Bug> pendingVerifyBugs = bugMapper.selectList(
                new LambdaQueryWrapper<Bug>()
                        .eq(Bug::getCreatorId, userId)
                        .notIn(Bug::getStatus, "unfixed", "closed")
                        .orderByDesc(Bug::getUpdatedAt));
        for (Bug b : pendingVerifyBugs) {
            if (b.getTaskId() != null) b.setTask(taskMapper.selectById(b.getTaskId()));
        }

        // 我创建的未修复Bug
        List<Bug> unfixedBugs = bugMapper.selectList(
                new LambdaQueryWrapper<Bug>()
                        .eq(Bug::getCreatorId, userId)
                        .eq(Bug::getStatus, "unfixed")
                        .orderByDesc(Bug::getUpdatedAt));
        for (Bug b : unfixedBugs) {
            if (b.getTaskId() != null) b.setTask(taskMapper.selectById(b.getTaskId()));
        }

        // 批量加载 Bug 相关人员，避免 N+1
        java.util.Set<Long> bugUserIds = new java.util.HashSet<>();
        for (Bug b : pendingVerifyBugs) {
            if (b.getCreatorId() != null) bugUserIds.add(b.getCreatorId());
            if (b.getAssigneeId() != null) bugUserIds.add(b.getAssigneeId());
        }
        for (Bug b : unfixedBugs) {
            if (b.getCreatorId() != null) bugUserIds.add(b.getCreatorId());
            if (b.getAssigneeId() != null) bugUserIds.add(b.getAssigneeId());
        }
        java.util.Map<Long, User> bugUserMap = new java.util.HashMap<>();
        if (!bugUserIds.isEmpty()) {
            for (Long uid : bugUserIds) {
                User user = userMapper.selectById(uid);
                if (user != null) bugUserMap.put(uid, user);
            }
        }

        // 缓存 Bug 关联任务的需求信息
        java.util.Map<Long, Requirement> bugReqCache = new java.util.HashMap<>();

        long totalTesting = allTestingTasks.size();
        long pendingVerify = pendingVerifyBugs.size();
        long unfixedCount = unfixedBugs.size();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", Map.of("totalTesting", totalTesting, "pendingVerify", pendingVerify, "unfixed", unfixedCount));
        result.put("tasks", allTestingTasks.stream().map(t -> {
            Map<String, Object> m = taskToMap(t);
            List<String> creators = bugCreatorsByTask.get(t.getId());
            m.put("bugCreators", creators != null && !creators.isEmpty() ? String.join(" | ", creators) : null);
            return m;
        }).collect(Collectors.toList()));
        result.put("pendingVerifyBugs", pendingVerifyBugs.stream().map(b -> bugToMap(b, bugUserMap, bugReqCache)).collect(Collectors.toList()));
        result.put("unfixedBugs", unfixedBugs.stream().map(b -> bugToMap(b, bugUserMap, bugReqCache)).collect(Collectors.toList()));
        return result;
    }

    /** 进度异常任务：近两周进度无变化且当前进度未达 100% */
    public List<Map<String, Object>> getProgressAnomalies() {
        List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>()
                        .ne(Task::getStatus, "closed")
                        .lt(Task::getProgress, 100)
                        .isNotNull(Task::getProgress));
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
        List<TaskProgressHistory> histories = taskProgressHistoryMapper.selectList(
                new LambdaQueryWrapper<TaskProgressHistory>()
                        .in(TaskProgressHistory::getTaskId, taskIds)
                        .orderByDesc(TaskProgressHistory::getCreatedAt));
        Map<Long, List<TaskProgressHistory>> historyByTask = histories.stream()
                .collect(Collectors.groupingBy(TaskProgressHistory::getTaskId));

        Set<Long> reqIds = tasks.stream()
                .map(Task::getRequirementId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Requirement> reqMap = new HashMap<>();
        for (Long reqId : reqIds) {
            Requirement r = requirementMapper.selectById(reqId);
            if (r != null) reqMap.put(reqId, r);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> anomalies = new ArrayList<>();
        for (Task t : tasks) {
            List<TaskProgressHistory> hlist = historyByTask.getOrDefault(t.getId(), Collections.emptyList());
            if (hlist.isEmpty()) continue;
            TaskProgressHistory latest = hlist.get(0);
            TaskProgressHistory previous = hlist.stream()
                    .filter(h -> h.getCreatedAt() != null && h.getCreatedAt().isBefore(now.minusDays(7)))
                    .findFirst()
                    .orElse(null);
            if (previous == null) continue;
            if (latest.getProgress() != null && latest.getProgress().equals(previous.getProgress())) {
                Requirement r = reqMap.get(t.getRequirementId());
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("requirementNumber", r != null ? (r.getNumber() != null ? r.getNumber() : r.getRequirementId()) : "-");
                m.put("system", r != null ? r.getSystem() : "-");
                m.put("description", r != null ? r.getDescription() : "-");
                m.put("taskTitle", t.getTitle());
                m.put("progress", latest.getProgress());
                anomalies.add(m);
            }
        }
        return anomalies;
    }

    private Map<String, Object> bugToMap(Bug b, java.util.Map<Long, User> userMap, java.util.Map<Long, Requirement> reqCache) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("title", b.getTitle());
        m.put("severity", b.getSeverity());
        m.put("status", b.getStatus());
        m.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
        if (b.getCreatorId() != null) {
            User creator = userMap.get(b.getCreatorId());
            m.put("creatorId", b.getCreatorId());
            m.put("creatorName", creator != null ? creator.getName() : null);
        }
        if (b.getAssigneeId() != null) {
            User assignee = userMap.get(b.getAssigneeId());
            m.put("assigneeId", b.getAssigneeId());
            m.put("assigneeName", assignee != null ? assignee.getName() : null);
        }
        if (b.getTaskId() != null) {
            Task t = b.getTask();
            m.put("taskId", b.getTaskId());
            m.put("taskTitle", t != null ? t.getTitle() : null);
            if (t != null && t.getTerminal() != null) {
                m.put("terminal", t.getTerminal());
            }
            if (t != null && t.getRequirementId() != null) {
                Requirement r = reqCache.computeIfAbsent(t.getRequirementId(), id -> requirementMapper.selectById(id));
                if (r != null) {
                    m.put("reqNumber", r.getNumber() != null ? r.getNumber() : r.getRequirementId());
                    m.put("reqId", r.getRequirementId());
                    m.put("system", r.getSystem());
                }
            }
        } else if (b.getRequirementId() != null) {
            Requirement r = reqCache.computeIfAbsent(b.getRequirementId(), id -> requirementMapper.selectById(id));
            if (r != null) {
                m.put("reqNumber", r.getNumber() != null ? r.getNumber() : r.getRequirementId());
                m.put("reqId", r.getRequirementId());
                m.put("system", r.getSystem());
            }
        }
        return m;
    }

    private Map<String, Object> taskToMap(Task t) {
        fillTaskUser(t);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("title", t.getTitle());
        m.put("status", t.getStatus());
        m.put("assignee", t.getAssignee() != null ? t.getAssignee().getName() : "-");
        m.put("tester", t.getTester() != null ? t.getTester().getName() : "-");
        m.put("testerId", t.getTesterId());
        m.put("deadline", t.getDeadline() != null ? t.getDeadline().toString() : null);
        m.put("progress", t.getProgress());
        m.put("terminal", t.getTerminal());
        if (t.getRequirementId() != null) {
            Requirement r = requirementMapper.selectById(t.getRequirementId());
            if (r != null) {
                m.put("reqNumber", r.getNumber() != null ? r.getNumber() : r.getRequirementId());
                m.put("reqId", r.getRequirementId());
                m.put("system", r.getSystem());
            }
        }
        if (t.getDeadline() != null) {
            m.put("overdueDays", !"closed".equals(t.getStatus()) && t.getDeadline().isBefore(LocalDateTime.now())
                    ? LocalDateTime.now().toLocalDate().toEpochDay() - t.getDeadline().toLocalDate().toEpochDay() : 0);
        } else {
            m.put("overdueDays", 0);
        }
        return m;
    }

    private Map<String, Object> boardCol(List<Task> tasks, String status, String label) {
        Map<String, Object> col = new LinkedHashMap<>();
        col.put("status", status);
        col.put("label", label);
        col.put("count", tasks.stream().filter(t -> status.equals(t.getStatus())).count());
        return col;
    }

    private void fillTaskUser(Task t) {
        if (t.getAssigneeId() != null) t.setAssignee(userMapper.selectById(t.getAssigneeId()));
        if (t.getTesterId() != null) t.setTester(userMapper.selectById(t.getTesterId()));
    }
}
