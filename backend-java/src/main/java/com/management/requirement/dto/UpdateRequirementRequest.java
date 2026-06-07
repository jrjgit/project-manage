package com.management.requirement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "更新需求请求")
@Data
public class UpdateRequirementRequest {
    /** 需求描述 */
    private String description;
    /** 备注 */
    private String notes;
    /** 关联系统 */
    private String system;
    /** 项目ID */
    private Long projectId;
    /** 负责人ID */
    private Long personId;
    /** 负责人姓名 */
    private String personName;
    /** 优先级 */
    private String priority;
    /** 需求状态 */
    private String status;
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
    /** 需求编号 */
    private String requirementId;
    /** 需求序号 */
    private String number;
    /** 关联迭代ID */
    private String iterationId;
    /** 项目类型 */
    private String projectType;
    /** 计划完成时间 */
    private String plannedCompletionTime;
    /** 业务状态 */
    private String businessStatus;
}
