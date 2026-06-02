package com.management.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final RequirementMapper requirementMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    /** 年度营收统计 */
    public Map<String, Object> revenueStats(int year) {
        List<Requirement> all = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .ge(Requirement::getCreatedAt, year + "-01-01")
                        .lt(Requirement::getCreatedAt, (year + 1) + "-01-01"));

        long totalCount = all.size();
        long plannedCount = all.stream().filter(r -> "planned".equals(r.getStatus())).count();
        long doneCount = all.stream().filter(r -> "released".equals(r.getStatus())).count();
        long inProgressCount = all.stream().filter(r -> "in_progress".equals(r.getStatus())).count();
        long testingCount = all.stream().filter(r ->
                "integration_test".equals(r.getStatus()) || "business_test".equals(r.getStatus())).count();

        int[] monthlyCreated = new int[12];
        int[] monthlyDone = new int[12];
        for (Requirement r : all) {
            int m = r.getCreatedAt().getMonthValue() - 1;
            monthlyCreated[m]++;
            if ("released".equals(r.getStatus()) && r.getReleaseTime() != null) {
                int rm = r.getReleaseTime().getMonthValue() - 1;
                monthlyDone[rm]++;
            }
        }

        List<Map<String, Object>> monthlyCreatedList = new ArrayList<>();
        List<Map<String, Object>> monthlyDoneList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Map<String, Object> createdItem = new LinkedHashMap<>();
            createdItem.put("month", i + 1);
            createdItem.put("count", monthlyCreated[i]);
            monthlyCreatedList.add(createdItem);

            Map<String, Object> doneItem = new LinkedHashMap<>();
            doneItem.put("month", i + 1);
            doneItem.put("count", monthlyDone[i]);
            monthlyDoneList.add(doneItem);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_created", totalCount);
        result.put("pending_count", plannedCount);
        result.put("done_count", doneCount);
        result.put("testing_count", testingCount);
        result.put("monthly_created", monthlyCreatedList);
        result.put("monthly_done", monthlyDoneList);
        return result;
    }

    /** 人员绩效统计 */
    public Map<String, Object> performanceStats(int year, int month) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getRole, "dev", "dev_lead"));
        List<Map<String, Object>> stats = new ArrayList<>();

        for (User u : users) {
            List<Task> tasks = taskMapper.selectList(
                    new LambdaQueryWrapper<Task>()
                            .eq(Task::getAssigneeId, u.getId()));
            long inProgress = tasks.stream()
                    .filter(t -> "developing".equals(t.getStatus()) || "testing".equals(t.getStatus())).count();
            long overdue = tasks.stream()
                    .filter(t -> !"closed".equals(t.getStatus())
                            && t.getDeadline() != null
                            && t.getDeadline().isBefore(java.time.LocalDateTime.now()))
                    .count();
            long done = tasks.stream()
                    .filter(t -> "closed".equals(t.getStatus()))
                    .count();
            double totalPerformance = tasks.stream()
                    .filter(t -> t.getPerformance() != null && !t.getPerformance().isBlank())
                    .mapToDouble(t -> { try { return Double.parseDouble(t.getPerformance()); } catch (Exception e) { return 0; } })
                    .sum();

            if (tasks.isEmpty()) continue;

            Map<String, Object> userStat = new LinkedHashMap<>();
            userStat.put("user_id", u.getId());
            userStat.put("user_name", u.getName());
            userStat.put("in_progress", inProgress);
            userStat.put("overdue", overdue);
            userStat.put("done", done);
            userStat.put("performance", totalPerformance);
            stats.add(userStat);
        }
        return Map.of("users", stats);
    }
}
