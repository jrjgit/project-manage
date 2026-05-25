package com.management.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.notification.email-enable", havingValue = "true")
@RequiredArgsConstructor
public class EmailNotifier implements Notifier {
    private final JavaMailSender mailSender;

    @Override
    public void notify(String email, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("Management System Notification");
            mail.setText(message);
            mailSender.send(mail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
        }
    }
}
