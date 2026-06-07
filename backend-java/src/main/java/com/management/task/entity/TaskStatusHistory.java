package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "任务状态历史")
@Data
@TableName("task_status_histories")
public class TaskStatusHistory {
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 任务ID */
    private Long taskId;
    /** 变更前状态 */
    private String fromStatus;
    /** 变更后状态 */
    private String toStatus;
    /** 操作人用户ID */
    private Long changedBy;
    /** 操作人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User user;
    /** 变更时间 */
    private LocalDateTime changedAt;
    /** 变更备注 */
    private String comment;
}
