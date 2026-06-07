package com.management.common.notification;

import com.management.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AsyncNotificationSender {

    private final MultiNotifier multiNotifier;

    @Async("notifyExecutor")
    public void sendAsync(List<User> targets, String operatorName, String message) {
        sendAsync(targets, operatorName, message, "system", null);
    }

    @Async("notifyExecutor")
    public void sendAsync(List<User> targets, String operatorName, String message, String type, Long relatedId) {
        if (targets == null) return;
        java.util.Set<Long> sent = new java.util.HashSet<>();
        for (User target : targets) {
            if (target == null || target.getId() == null || sent.contains(target.getId())) continue;
            sent.add(target.getId());
            if (operatorName != null && operatorName.equals(target.getName())) continue;
            multiNotifier.notify(target.getName(), message, type, relatedId);
        }
    }
}
