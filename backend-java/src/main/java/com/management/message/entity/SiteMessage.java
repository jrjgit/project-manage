package com.management.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "站内消息")
@Data
@TableName("site_messages")
public class SiteMessage {
    /** 消息ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 接收用户ID */
    private Long userId;
    /** 消息标题 */
    private String title;
    /** 消息内容 */
    private String content;
    /** 消息类型 */
    private String type;
    /** 关联业务ID */
    private Long relatedId;
    /** 是否已读 */
    private Boolean isRead;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 阅读时间 */
    private LocalDateTime readAt;
}
