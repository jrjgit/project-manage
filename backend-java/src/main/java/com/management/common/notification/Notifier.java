package com.management.common.notification;

public interface Notifier {
    void notify(String recipient, String message);

    default void notify(String recipient, String message, String type, Long relatedId) {
        notify(recipient, message);
    }
}
