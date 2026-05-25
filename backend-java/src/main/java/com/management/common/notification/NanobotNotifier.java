package com.management.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.notification.nanobot-enable", havingValue = "true", matchIfMissing = true)
public class NanobotNotifier implements Notifier {

    private final String nanobotPath;

    public NanobotNotifier(@Value("${app.nanobot-path:nanobot}") String nanobotPath) {
        this.nanobotPath = nanobotPath;
    }

    @Override
    public void notify(String userName, String message) {
        try {
            String instruction = "通知用户 " + userName + "：" + message;
            ProcessBuilder pb = new ProcessBuilder(nanobotPath, "agent", "-m", instruction);
            Process p = pb.start();
            boolean finished = p.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                log.warn("Nanobot timeout for user {}", userName);
                return;
            }
            if (p.exitValue() != 0) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    log.warn("Nanobot error: {}", br.readLine());
                }
            }
        } catch (Exception e) {
            log.error("Nanobot notify failed: {}", e.getMessage());
        }
    }
}
