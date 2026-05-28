package com.management.common.notification;

import com.management.message.entity.SiteMessage;
import com.management.message.mapper.SiteMessageMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SiteMessageNotifier implements Notifier {

    private final SiteMessageMapper siteMessageMapper;
    private final UserMapper userMapper;

    @Override
    public void notify(String recipient, String message) {
        notify(recipient, message, detectType(message), null);
    }

    @Override
    public void notify(String recipient, String message, String type, Long relatedId) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getName, recipient));
        if (user == null) {
            log.warn("SiteMessageNotifier: user not found for recipient={}", recipient);
            return;
        }

        String title = message.length() > 50 ? message.substring(0, 50) + "..." : message;

        SiteMessage msg = new SiteMessage();
        msg.setUserId(user.getId());
        msg.setTitle(title);
        msg.setContent(message);
        msg.setType(type);
        msg.setRelatedId(relatedId);
        msg.setIsRead(false);
        siteMessageMapper.insert(msg);

        log.debug("SiteMessage saved for user={}, title={}, type={}, relatedId={}",
                user.getName(), title, type, relatedId);
    }

    private String detectType(String message) {
        if (message.startsWith("任务")) return "task";
        if (message.startsWith("Bug")) return "bug";
        if (message.startsWith("需求")) return "requirement";
        return "system";
    }
}
