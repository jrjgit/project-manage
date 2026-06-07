package com.management.common.notification;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MultiNotifier implements Notifier {
    private final List<Notifier> delegates = new ArrayList<>();

    public void addDelegate(Notifier notifier) {
        this.delegates.add(notifier);
    }

    @Override
    public void notify(String recipient, String message) {
        for (Notifier n : delegates) {
            n.notify(recipient, message);
        }
    }

    @Override
    public void notify(String recipient, String message, String type, Long relatedId) {
        for (Notifier n : delegates) {
            n.notify(recipient, message, type, relatedId);
        }
    }
}
