package com.management.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.bug.mapper.BugMapper;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final RequirementMapper requirementMapper;
    private final TaskMapper taskMapper;
    private final BugMapper bugMapper;
    private final UserMapper userMapper;

    /** 运维营收统计 — 只统计 project_type = ops 的需求 */
    public Map<String, Object> revenueStats(int year, Long projectId) {
        LambdaQueryWrapper<Requirement> q = new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getProjectType, "ops")
                .ge(Requirement::getCreatedAt, year + "-01-01")
                .lt(Requirement::getCreatedAt, (year + 1) + "-01-01");
        if (projectId != null) q.eq(Requirement::getProjectId, projectId);

        List<Requirement> all = requirementMapper.selectList(q);

        double totalCreatedAmount = 0;
        double totalDoneAmount = 0;

        double[] monthlyCreatedAmount = new double[12];
        double[] monthlyDoneAmount = new double[12];

        for (Requirement r : all) {
            double amt = parseDoubleSafe(r.getTotalPrice());
            totalCreatedAmount += amt;

            int m = r.getCreatedAt().getMonthValue() - 1;
            monthlyCreatedAmount[m] += amt;

            if ("released".equals(r.getStatus()) && r.getUpdatedAt() != null) {
                totalDoneAmount += amt;
                int rm = r.getUpdatedAt().getMonthValue() - 1;
                monthlyDoneAmount[rm] += amt;
            }
        }

        List<Map<String, Object>> monthlyCreatedList = new ArrayList<>();
        List<Map<String, Object>> monthlyDoneList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Map<String, Object> createdItem = new LinkedHashMap<>();
            createdItem.put("month", i + 1);
            createdItem.put("amount", monthlyCreatedAmount[i]);
            monthlyCreatedList.add(createdItem);

            Map<String, Object> doneItem = new LinkedHashMap<>();
            doneItem.put("month", i + 1);
            doneItem.put("amount", monthlyDoneAmount[i]);
            monthlyDoneList.add(doneItem);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_created", totalCreatedAmount);
        result.put("total_done", totalDoneAmount);
        result.put("monthly_created", monthlyCreatedList);
        result.put("monthly_done", monthlyDoneList);
        return result;
    }

    /** 人员绩效统计 */
    public Map<String, Object> performanceStats(String filterType, int year, int month, String startDate, String endDate) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getRole, "dev", "dev_lead"));
        if (users.isEmpty()) {
            return Map.of("users", Collections.emptyList());
        }

        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        List<Long> userIds = new ArrayList<>(userMap.keySet());

        // 绩效产值按任务最后更新时间筛选
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;
        if ("range".equals(filterType) && startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
            rangeStart = LocalDate.parse(startDate).atStartOfDay();
            rangeEnd = LocalDate.parse(endDate).plusDays(1).atStartOfDay(); // 不包含结束日次日
        } else if ("month".equals(filterType)) {
            rangeStart = LocalDateTime.of(year, month, 1, 0, 0);
            rangeEnd = rangeStart.plusMonths(1);
        } else {
            // 按年
            rangeStart = LocalDateTime.of(year, 1, 1, 0, 0);
            rangeEnd = rangeStart.plusYears(1);
        }

        // 一次性查询所有开发人员的任务
        List<Task> allTasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().in(Task::getAssigneeId, userIds));
        Map<Long, List<Task>> tasksByUser = allTasks.stream().collect(Collectors.groupingBy(Task::getAssigneeId));

        // 批量加载任务关联的未修复 Bug 数量
        List<Long> allTaskIds = allTasks.stream().map(Task::getId).collect(Collectors.toList());
        java.util.Map<Long, Long> pendingBugsByTask = new java.util.HashMap<>();
        if (!allTaskIds.isEmpty()) {
            List<com.management.bug.entity.Bug> taskBugs = bugMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.management.bug.entity.Bug>()
                            .in(com.management.bug.entity.Bug::getTaskId, allTaskIds)
                            .eq(com.management.bug.entity.Bug::getStatus, "unfixed"));
            for (com.management.bug.entity.Bug b : taskBugs) {
                if (b.getTaskId() != null) {
                    pendingBugsByTask.merge(b.getTaskId(), 1L, Long::sum);
                }
            }
        }

        // 缓存需求信息
        java.util.Map<Long, Requirement> reqCache = new java.util.HashMap<>();
        java.util.Map<Long, Double> priceCache = new java.util.HashMap<>();

        List<Map<String, Object>> stats = new ArrayList<>();
        for (User u : users) {
            List<Task> tasks = tasksByUser.getOrDefault(u.getId(), Collections.emptyList());
            if (tasks.isEmpty()) continue;

            double inProgressLoad = 0;
            double performanceValue = 0;
            List<Map<String, Object>> taskDetails = new ArrayList<>();

            for (Task t : tasks) {
                double perf = parseDoubleSafe(t.getPerformance());
                double progress = t.getProgress() != null ? t.getProgress() : 0;
                String status = t.getStatus();

                // 进行中任务负载：状态不等于 closed
                if (!"closed".equals(status)) {
                    inProgressLoad += perf * (1 - progress / 100.0);
                }

                // 绩效产值：状态为 closed，且按任务最后更新时间落在筛选范围内
                if ("closed".equals(status)) {
                    LocalDateTime updatedAt = t.getUpdatedAt();
                    if (updatedAt != null && !updatedAt.isBefore(rangeStart) && updatedAt.isBefore(rangeEnd)) {
                        double price = getDevPrice(t.getRequirementId(), priceCache);
                        performanceValue += perf * price;
                    }
                }

                Requirement req = getRequirement(t.getRequirementId(), reqCache);
                Map<String, Object> taskInfo = new LinkedHashMap<>();
                taskInfo.put("id", t.getId());
                taskInfo.put("requirement_number", req != null ? req.getNumber() : "-");
                taskInfo.put("system", req != null ? req.getSystem() : "-");
                taskInfo.put("terminal", t.getTerminal());
                taskInfo.put("description", t.getTitle());
                taskInfo.put("assignee_name", u.getName());
                taskInfo.put("progress", progress);
                taskInfo.put("status", status);
                taskInfo.put("pending_bugs", pendingBugsByTask.getOrDefault(t.getId(), 0L));
                taskDetails.add(taskInfo);
            }

            Map<String, Object> userStat = new LinkedHashMap<>();
            userStat.put("user_id", u.getId());
            userStat.put("user_name", u.getName());
            userStat.put("in_progress_load", round2(inProgressLoad));
            userStat.put("performance_value", round2(performanceValue));
            userStat.put("tasks", taskDetails);
            stats.add(userStat);
        }
        return Map.of("users", stats);
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return 0;
        try { return Double.parseDouble(value); } catch (Exception e) { return 0; }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Requirement getRequirement(Long requirementId, java.util.Map<Long, Requirement> cache) {
        if (requirementId == null) return null;
        return cache.computeIfAbsent(requirementId, id -> requirementMapper.selectById(id));
    }

    private double getDevPrice(Long requirementId, java.util.Map<Long, Double> cache) {
        if (requirementId == null) return 0;
        if (cache.containsKey(requirementId)) return cache.get(requirementId);
        Requirement r = requirementMapper.selectById(requirementId);
        double price = r != null ? parseDoubleSafe(r.getDevPrice()) : 0;
        cache.put(requirementId, price);
        return price;
    }
}
