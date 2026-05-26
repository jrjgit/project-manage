package com.management.message;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.management.common.result.Result;
import com.management.message.entity.SiteMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class SiteMessageController {

    private final SiteMessageService siteMessageService;

    @GetMapping
    public Result<IPage<SiteMessage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        return Result.ok(siteMessageService.listMessages(page, size, type, isRead));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.ok(Map.of("count", siteMessageService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        siteMessageService.markRead(id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        siteMessageService.markAllRead();
        return Result.ok();
    }
}
