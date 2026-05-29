package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_progress_history")
public class TaskProgressHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Integer progress;
    private String comment;
    private Long createdBy;
    @TableField(exist = false)
    private User user;
    private LocalDateTime createdAt;
}
