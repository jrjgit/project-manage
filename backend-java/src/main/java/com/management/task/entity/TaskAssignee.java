package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_assignees")
public class TaskAssignee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long userId;
    @TableField(exist = false)
    private User user;
    private String platform;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
