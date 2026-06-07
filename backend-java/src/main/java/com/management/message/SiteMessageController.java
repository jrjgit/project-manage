package com.management.message;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.management.common.result.Result;
import com.management.message.entity.SiteMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "消息管理", description = "站内消息的查看和管理")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class SiteMessageController {

    private final SiteMessageService siteMessageService;

    @Operation(summary = "获取消息列表")
    /**
     * 获取站内消息列表（分页）
     */
    @GetMapping
    public Result<IPage<SiteMessage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        return Result.ok(siteMessageService.listMessages(page, size, type, isRead));
    }

    @Operation(summary = "获取未读消息数")
    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.ok(Map.of("count", siteMessageService.getUnreadCount()));
    }

    @Operation(summary = "标记单条消息已读")
    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        siteMessageService.markRead(id);
        return Result.ok();
    }

    @Operation(summary = "全部标记已读")
    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        siteMessageService.markAllRead();
        return Result.ok();
    }
}
