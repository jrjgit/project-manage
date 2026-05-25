package com.management.bug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.task.entity.Task;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bugs")
public class Bug {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String severity;
    private String status;
    private Long taskId;
    @TableField(exist = false)
    private Task task;
    private Long creatorId;
    @TableField(exist = false)
    private User creator;
    private Long assigneeId;
    @TableField(exist = false)
    private User assignee;
    private String fixComment;
    private String reopenReason;
    private Long requirementId;
    private Long developerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
