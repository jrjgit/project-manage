package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_status_histories")
public class TaskStatusHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String fromStatus;
    private String toStatus;
    private Long changedBy;
    @TableField(exist = false)
    private User user;
    private LocalDateTime changedAt;
    private String comment;
}
