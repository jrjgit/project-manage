package com.management.common.notification;

import com.management.bug.entity.Bug;
import com.management.requirement.entity.Requirement;
import com.management.task.entity.Task;
import com.management.user.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    private final MultiNotifier multiNotifier;
    private final LogNotifier logNotifier;
    private final SiteMessageNotifier siteMessageNotifier;
    private final NanobotNotifier nanobotNotifier;
    private final AsyncNotificationSender asyncSender;

    @Value("${app.notification.nanobot-enable:true}")
    private boolean nanobotEnable;
    @Value("${app.notification.async:true}")
    private boolean asyncEnabled;

    public NotificationService(MultiNotifier multiNotifier,
                                LogNotifier logNotifier,
                                SiteMessageNotifier siteMessageNotifier,
                                AsyncNotificationSender asyncSender,
                                @org.springframework.beans.factory.annotation.Autowired(required = false) NanobotNotifier nanobotNotifier) {
        this.multiNotifier = multiNotifier;
        this.logNotifier = logNotifier;
        this.siteMessageNotifier = siteMessageNotifier;
        this.asyncSender = asyncSender;
        this.nanobotNotifier = nanobotNotifier;
    }

    private static final Map<String, String> TASK_STATUS_LABELS = Map.of(
            "pending", "待受理",
            "developing", "开发中",
            "testing", "测试中",
            "closed", "已完成"
    );

    private static final Map<String, String> BUG_STATUS_LABELS = Map.of(
            "unfixed",   "未修复",
            "fixed",     "已修复",
            "not_a_bug", "开发确认非Bug",
            "closed",    "已关闭"
    );

    private static final Map<String, String> REQUIREMENT_STATUS_LABELS = Map.of(
            "planned", "未开始",
            "pending_task", "任务待分配",
            "in_progress", "开发中",
            "integration_test", "综合测试",
            "business_test", "业务测试",
            "pending_release", "待发布",
            "released", "已发布",
            "closed", "已关闭"
    );

    @PostConstruct
    void init() {
        multiNotifier.addDelegate(logNotifier);
        multiNotifier.addDelegate(siteMessageNotifier);
        if (nanobotEnable && nanobotNotifier != null) multiNotifier.addDelegate(nanobotNotifier);
    }

    public void emitTaskEvent(Task task, String oldStatus, String newStatus,
                               String operatorName, List<User> targets, String comment) {
        String msg = buildTaskMessage(task.getTitle(), oldStatus, newStatus, operatorName, comment);
        if (asyncEnabled) {
            asyncSender.sendAsync(targets, operatorName, msg, "task", task.getId());
        } else {
            sendToTargets(targets, operatorName, msg, "task", task.getId());
        }
    }

    public void emitBugEvent(Bug bug, String oldStatus, String newStatus,
                              String operatorName, List<User> targets, String comment) {
        String msg = buildBugMessage(bug.getTitle(), oldStatus, newStatus, operatorName, comment);
        if (asyncEnabled) {
            asyncSender.sendAsync(targets, operatorName, msg, "bug", bug.getId());
        } else {
            sendToTargets(targets, operatorName, msg, "bug", bug.getId());
        }
    }

    public void emitRequirementEvent(Requirement req, String oldStatus, String newStatus,
                                      String operatorName, List<User> targets, String comment) {
        String msg = buildRequirementMessage(req.getDescription(), oldStatus, newStatus, operatorName, comment);
        if (asyncEnabled) {
            asyncSender.sendAsync(targets, operatorName, msg, "requirement", req.getId());
        } else {
            sendToTargets(targets, operatorName, msg, "requirement", req.getId());
        }
    }

    public void emitGenericEvent(String message, String operatorName, List<User> targets) {
        emitGenericEvent(message, operatorName, targets, "system", null);
    }

    public void emitGenericEvent(String message, String operatorName, List<User> targets, String type, Long relatedId) {
        if (asyncEnabled) {
            asyncSender.sendAsync(targets, operatorName, message, type, relatedId);
        } else {
            sendToTargets(targets, operatorName, message, type, relatedId);
        }
    }

    private void sendToTargets(List<User> targets, String operatorName, String message) {
        sendToTargets(targets, operatorName, message, "system", null);
    }

    private void sendToTargets(List<User> targets, String operatorName, String message, String type, Long relatedId) {
        if (targets == null) return;
        java.util.Set<Long> sent = new java.util.HashSet<>();
        for (User target : targets) {
            if (target == null || target.getId() == null || sent.contains(target.getId())) continue;
            sent.add(target.getId());
            if (operatorName != null && operatorName.equals(target.getName())) continue;
            multiNotifier.notify(target.getName(), message, type, relatedId);
        }
    }

    private String buildTaskMessage(String title, String oldStatus, String newStatus,
                                     String operator, String comment) {
        String oldLabel = TASK_STATUS_LABELS.getOrDefault(oldStatus, oldStatus);
        String newLabel = TASK_STATUS_LABELS.getOrDefault(newStatus, newStatus);
        StringBuilder sb = new StringBuilder();
        sb.append("任务【").append(title).append("】状态已从【")
                .append(oldLabel).append("】变更为【").append(newLabel)
                .append("】，操作人：").append(operator);
        if (comment != null && !comment.isBlank()) {
            sb.append("，备注：").append(comment);
        }
        sb.append("。请及时处理。");
        return sb.toString();
    }

    private String buildBugMessage(String title, String oldStatus, String newStatus,
                                    String operator, String comment) {
        String oldLabel = BUG_STATUS_LABELS.getOrDefault(oldStatus, oldStatus);
        String newLabel = BUG_STATUS_LABELS.getOrDefault(newStatus, newStatus);
        StringBuilder sb = new StringBuilder();
        sb.append("Bug【").append(title).append("】状态已从【")
                .append(oldLabel).append("】变更为【").append(newLabel)
                .append("】，操作人：").append(operator);
        if (comment != null && !comment.isBlank()) {
            sb.append("，备注：").append(comment);
        }
        sb.append("。请及时处理。");
        return sb.toString();
    }

    private String buildRequirementMessage(String description, String oldStatus, String newStatus,
                                            String operator, String comment) {
        String oldLabel = REQUIREMENT_STATUS_LABELS.getOrDefault(oldStatus, oldStatus);
        String newLabel = REQUIREMENT_STATUS_LABELS.getOrDefault(newStatus, newStatus);
        StringBuilder sb = new StringBuilder();
        sb.append("需求【").append(description).append("】状态已从【")
                .append(oldLabel).append("】变更为【").append(newLabel)
                .append("】，操作人：").append(operator);
        if (comment != null && !comment.isBlank()) {
            sb.append("，备注：").append(comment);
        }
        sb.append("。请及时处理。");
        return sb.toString();
    }
}
