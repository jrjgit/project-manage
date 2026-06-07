package com.management.bug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Bug状态历史")
@Data
@TableName("bug_status_histories")
public class BugStatusHistory {
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** Bug ID */
    private Long bugId;
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
