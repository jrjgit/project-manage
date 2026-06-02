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

    /** 年度营收统计 — 使用需求的 total_price（总金额） */
    public Map<String, Object> revenueStats(int year) {
        List<Requirement> all = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .ge(Requirement::getCreatedAt, year + "-01-01")
                        .lt(Requirement::getCreatedAt, (year + 1) + "-01-01"));

        double totalAmount = 0;
        double pendingAmount = 0;
        double doneAmount = 0;
        double testingAmount = 0;

        double[] monthlyCreatedAmount = new double[12];
        double[] monthlyDoneAmount = new double[12];

        for (Requirement r : all) {
            double amt = parseDoubleSafe(r.getTotalPrice());
            totalAmount += amt;

            int m = r.getCreatedAt().getMonthValue() - 1;
            monthlyCreatedAmount[m] += amt;

            if ("planned".equals(r.getStatus())) pendingAmount += amt;
            else if ("released".equals(r.getStatus())) {
                doneAmount += amt;
                if (r.getReleaseTime() != null) {
                    int rm = r.getReleaseTime().getMonthValue() - 1;
                    monthlyDoneAmount[rm] += amt;
                }
            } else if ("integration_test".equals(r.getStatus()) || "business_test".equals(r.getStatus())) {
                testingAmount += amt;
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
        result.put("total_created", totalAmount);
        result.put("pending_count", pendingAmount);
        result.put("done_count", doneAmount);
        result.put("testing_count", testingAmount);
        result.put("monthly_created", monthlyCreatedList);
        result.put("monthly_done", monthlyDoneList);
        return result;
    }

    /** 人员绩效统计 — 绩效金额 = 任务绩效 × 需求单价 */
    public Map<String, Object> performanceStats(int year, int month) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getRole, "dev", "dev_lead"));
        List<Map<String, Object>> stats = new ArrayList<>();
        // 缓存需求单价
        java.util.Map<Long, Double> priceCache = new java.util.HashMap<>();

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

            double perfAmount = 0;
            for (Task t : tasks) {
                double perf = parseDoubleSafe(t.getPerformance());
                if (perf <= 0) continue;
                double price = getDevPrice(t.getRequirementId(), priceCache);
                perfAmount += perf * price;
            }

            if (tasks.isEmpty()) continue;

            Map<String, Object> userStat = new LinkedHashMap<>();
            userStat.put("user_id", u.getId());
            userStat.put("user_name", u.getName());
            userStat.put("in_progress", inProgress);
            userStat.put("overdue", overdue);
            userStat.put("done", done);
            userStat.put("performance", perfAmount);
            stats.add(userStat);
        }
        return Map.of("users", stats);
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return 0;
        try { return Double.parseDouble(value); } catch (Exception e) { return 0; }
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
