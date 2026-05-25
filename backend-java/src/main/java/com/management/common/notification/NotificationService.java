package com.management.common.notification;

import com.management.bug.entity.Bug;
import com.management.task.entity.Task;
import com.management.user.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    private final MultiNotifier multiNotifier;
    private final LogNotifier logNotifier;
    private final EmailNotifier emailNotifier;
    private final NanobotNotifier nanobotNotifier;
    private final AsyncNotificationSender asyncSender;

    @Value("${app.notification.email-enable:false}")
    private boolean emailEnable;
    @Value("${app.notification.nanobot-enable:true}")
    private boolean nanobotEnable;
    @Value("${app.notification.async:true}")
    private boolean asyncEnabled;

    public NotificationService(MultiNotifier multiNotifier,
                                LogNotifier logNotifier,
                                AsyncNotificationSender asyncSender,
                                @org.springframework.beans.factory.annotation.Autowired(required = false) EmailNotifier emailNotifier,
                                @org.springframework.beans.factory.annotation.Autowired(required = false) NanobotNotifier nanobotNotifier) {
        this.multiNotifier = multiNotifier;
        this.logNotifier = logNotifier;
        this.asyncSender = asyncSender;
        this.emailNotifier = emailNotifier;
        this.nanobotNotifier = nanobotNotifier;
    }

    private static final Map<String, String> TASK_STATUS_LABELS = Map.of(
            "pending", "待分配",
            "assigned_lead", "已分配",
            "developing", "开发中",
            "developed", "开发完成",
            "pending_test", "待测试",
            "testing", "测试中",
            "passed", "测试通过",
            "rejected", "打回修改",
            "closed", "已关闭"
    );

    private static final Map<String, String> BUG_STATUS_LABELS = Map.of(
            "new", "新建",
            "assigned", "已分配",
            "fixing", "修复中",
            "fixed", "已修复",
            "pending_verify", "待验证",
            "closed", "已关闭",
            "reopened", "重新打开"
    );

    @PostConstruct
    void init() {
        multiNotifier.addDelegate(logNotifier);
        if (emailEnable && emailNotifier != null) multiNotifier.addDelegate(emailNotifier);
        if (nanobotEnable && nanobotNotifier != null) multiNotifier.addDelegate(nanobotNotifier);
    }

    public void emitTaskEvent(Task task, String oldStatus, String newStatus,
                               String operatorName, List<User> targets, String comment) {
        String msg = buildTaskMessage(task.getTitle(), oldStatus, newStatus, operatorName, comment);
        if (asyncEnabled) {
            asyncSender.sendAsync(targets, operatorName, msg);
        } else {
            sendToTargets(targets, operatorName, msg);
        }
    }

    public void emitBugEvent(Bug bug, String oldStatus, String newStatus,
                              String operatorName, List<User> targets, String comment) {
        String msg = buildBugMessage(bug.getTitle(), oldStatus, newStatus, operatorName, comment);
        if (asyncEnabled) {
            asyncSender.sendAsync(targets, operatorName, msg);
        } else {
            sendToTargets(targets, operatorName, msg);
        }
    }

    private void sendToTargets(List<User> targets, String operatorName, String message) {
        if (targets == null) return;
        for (User target : targets) {
            if (operatorName != null && operatorName.equals(target.getName())) continue;
            if (target.getEmail() != null && !target.getEmail().isBlank()) {
                multiNotifier.notify(target.getEmail(), message);
            }
            multiNotifier.notify(target.getName(), message);
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
}
