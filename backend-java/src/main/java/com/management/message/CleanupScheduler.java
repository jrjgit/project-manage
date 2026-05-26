package com.management.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final SiteMessageService siteMessageService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupReadMessages() {
        log.info("Starting cleanup of read messages older than 30 days");
        siteMessageService.cleanupReadMessages();
        log.info("Cleanup completed");
    }
}
