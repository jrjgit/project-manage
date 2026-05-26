package com.management.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.management.common.jwt.JwtUserDetails;
import com.management.message.entity.SiteMessage;
import com.management.message.mapper.SiteMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteMessageService {

    private final SiteMessageMapper siteMessageMapper;

    private Long currentUserId() {
        JwtUserDetails u = (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return u.getUserId();
    }

    public void createMessage(Long userId, String title, String content, String type, Long relatedId) {
        SiteMessage msg = new SiteMessage();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type);
        msg.setRelatedId(relatedId);
        msg.setIsRead(false);
        siteMessageMapper.insert(msg);
    }

    public IPage<SiteMessage> listMessages(int page, int size, String type, Boolean isRead) {
        LambdaQueryWrapper<SiteMessage> q = new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, currentUserId())
                .orderByDesc(SiteMessage::getCreatedAt);

        if (type != null && !type.isBlank() && !"all".equals(type)) {
            q.eq(SiteMessage::getType, type);
        }
        if (isRead != null) {
            q.eq(SiteMessage::getIsRead, isRead);
        }

        return siteMessageMapper.selectPage(new Page<>(page, size), q);
    }

    public long getUnreadCount() {
        return siteMessageMapper.selectCount(
                new LambdaQueryWrapper<SiteMessage>()
                        .eq(SiteMessage::getUserId, currentUserId())
                        .eq(SiteMessage::getIsRead, false));
    }

    @Transactional
    public void markRead(Long id) {
        SiteMessage msg = siteMessageMapper.selectById(id);
        if (msg != null && msg.getUserId().equals(currentUserId()) && Boolean.FALSE.equals(msg.getIsRead())) {
            msg.setIsRead(true);
            msg.setReadAt(LocalDateTime.now());
            siteMessageMapper.updateById(msg);
        }
    }

    @Transactional
    public void markAllRead() {
        List<SiteMessage> unread = siteMessageMapper.selectList(
                new LambdaQueryWrapper<SiteMessage>()
                        .eq(SiteMessage::getUserId, currentUserId())
                        .eq(SiteMessage::getIsRead, false));
        for (SiteMessage msg : unread) {
            msg.setIsRead(true);
            msg.setReadAt(LocalDateTime.now());
            siteMessageMapper.updateById(msg);
        }
    }

    public void cleanupReadMessages() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(30);
        siteMessageMapper.delete(
                new LambdaQueryWrapper<SiteMessage>()
                        .eq(SiteMessage::getIsRead, true)
                        .lt(SiteMessage::getReadAt, deadline));
    }
}
