package com.management.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogNotifier implements Notifier {
    @Override
    public void notify(String recipient, String message) {
        log.info("[Notify] to={}, msg={}", recipient, message);
    }
}
