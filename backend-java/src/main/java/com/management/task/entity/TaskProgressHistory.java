package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "任务进度历史")
@Data
@TableName("task_progress_history")
public class TaskProgressHistory {
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 任务ID */
    private Long taskId;
    /** 进度值（0-100） */
    private Integer progress;
    /** 备注 */
    private String comment;
    /** 操作人用户ID */
    private Long createdBy;
    /** 操作人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User user;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
