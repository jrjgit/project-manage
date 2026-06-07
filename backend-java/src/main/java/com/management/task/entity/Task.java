package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.management.project.entity.Project;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "任务")
@Data
@TableName("tasks")
public class Task {
    /** 任务ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 任务标题 */
    private String title;
    /** 任务描述 */
    private String description;
    /** 任务状态（todo=待办, developing=开发中, testing=测试中, done=已完成, rejected=已驳回） */
    private String status;
    /** 优先级（high=高, medium=中, low=低） */
    private String priority;
    /** 所属项目ID */
    private Long projectId;
    /** 关联项目对象（非数据库字段） */
    @TableField(exist = false)
    private Project project;
    /** 创建人用户ID */
    private Long creatorId;
    /** 创建人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User creator;
    /** 当前指派人用户ID（主要负责人） */
    private Long assigneeId;
    /** 当前指派人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User assignee;
    /** 开发组长用户ID */
    private Long devLeadId;
    /** 开发组长关联对象（非数据库字段） */
    @TableField(exist = false)
    private User devLead;
    /** 测试人员用户ID */
    private Long testerId;
    /** 测试人员关联对象（非数据库字段） */
    @TableField(exist = false)
    private User tester;
    /** 所有指派人列表（非数据库字段） */
    @TableField(exist = false)
    private List<TaskAssignee> assignees;
    /** 驳回原因 */
    private String rejectReason;
    /** 截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;
    /** 关联需求ID */
    private Long requirementId;
    /** 需求名称（非数据库字段，查询填充） */
    @TableField(exist = false)
    private String requirementName;
    /** 需求描述（非数据库字段，查询填充） */
    @TableField(exist = false)
    private String requirementDesc;
    /** 终端/技能要求 */
    private String terminal;
    /** 开发进度（百分比） */
    private Integer progress;
    /** 绩效工时 */
    private String performance;
    /** 测试绩效工时 */
    private String testPerformance;
    /** 关联迭代ID */
    private String iterationId;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
