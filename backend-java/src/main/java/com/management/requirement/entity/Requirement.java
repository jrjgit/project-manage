package com.management.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.project.entity.Project;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "需求")
@Data
@TableName("requirements")
public class Requirement {
    /** 需求主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 需求编号（自动生成，格式REQ-YYYY-XXX） */
    private String requirementId;
    /** 需求序号 */
    private String number;
    /** 需求描述 */
    private String description;
    /** 项目经理备注 */
    private String notes;
    /** 需求状态（pending=待处理, developing=开发中, testing=测试中, releasing=待发布, released=已发布） */
    private String status;
    /** 优先级 */
    private String priority;
    /** 关联系统名称 */
    @TableField("`system`")
    private String system;
    /** 所属项目ID */
    private Long projectId;
    /** 关联项目对象（非数据库字段） */
    @TableField(exist = false)
    private Project project;
    /** 项目类型 */
    private String projectType;
    /** 业务负责人用户ID */
    private Long personId;
    /** 业务负责人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User person;
    /** 业务负责人姓名 */
    private String personName;
    /** 总人天 */
    private String totalAmount;
    /** 总金额 */
    private String totalPrice;
    /** 开发人天 */
    private String devTotal;
    /** 开发单价 */
    private String devPrice;
    /** 测试人天 */
    private String testTotal;
    /** 测试单价 */
    private String testPrice;
    /** 上线时间 */
    private LocalDateTime releaseTime;
    /** 计划完成时间 */
    private LocalDateTime plannedCompletionTime;
    /** 开发组长用户ID */
    private Long devLeadId;
    /** 开发组长关联对象（非数据库字段） */
    @TableField(exist = false)
    private User devLead;
    /** 关联迭代ID */
    private String iterationId;
    /** 业务状态 */
    private String businessStatus;
    /** 需求文档路径 */
    private String documentPath;
    /** 需求文档文件名 */
    private String documentName;
    /** 需求文档大小（字节） */
    private Long documentSize;
    /** 进度备注 */
    private String progressNotes;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 迭代名称（非数据库字段） */
    @TableField(exist = false)
    private String iterationName;
    /** 开发进度（非数据库字段） */
    @TableField(exist = false)
    private Integer devProgress;
    /** 集成测试进度（非数据库字段） */
    @TableField(exist = false)
    private Integer integrationTestProgress;
    /** 业务测试进度（非数据库字段） */
    @TableField(exist = false)
    private Integer businessTestProgress;
    /** IT测试进度（非数据库字段） */
    @TableField(exist = false)
    private Integer itTestProgress;
    /** 测试进度（非数据库字段） */
    @TableField(exist = false)
    private Integer testProgress;
}
