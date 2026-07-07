package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "任务指派人")
@Data
@TableName("task_assignees")
public class TaskAssignee {
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 任务ID */
    private Long taskId;
    /** 用户ID */
    private Long userId;
    /** 关联用户对象（非数据库字段） */
    @TableField(exist = false)
    private User user;
    /** 平台信息 */
    private String platform;
    /** 分配状态 */
    private String status;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
