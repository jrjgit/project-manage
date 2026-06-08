package com.management.bug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.task.entity.Task;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Bug")
@Data
@TableName("bugs")
public class Bug {
    /** Bug ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** Bug标题 */
    private String title;
    /** Bug描述 */
    private String description;
    /** 严重程度（critical=致命, major=严重, minor=一般, suggestion=建议） */
    private String severity;
    /** Bug状态（unfixed=未修复, fixing=修复中, testing=测试中, verified=已验证, closed=已关闭） */
    private String status;
    /** 关联任务ID */
    private Long taskId;
    /** 关联任务对象（非数据库字段） */
    @TableField(exist = false)
    private Task task;
    /** 创建人用户ID */
    private Long creatorId;
    /** 创建人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User creator;
    /** 指派人用户ID */
    private Long assigneeId;
    /** 指派人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User assignee;
    /** 备注/处理说明 */
    private String remark;
    /** 关联需求ID */
    private Long requirementId;
    /** 测试类型（integration=集成测试, system=系统测试, acceptance=验收测试） */
    private String testType;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
