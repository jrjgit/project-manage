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
        msg.setType("system");
        msg.setIsRead(false);
        siteMessageMapper.insert(msg);

        log.debug("SiteMessage saved for user={}, title={}", user.getName(), title);
    }
}
