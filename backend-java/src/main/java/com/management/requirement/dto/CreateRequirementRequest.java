package com.management.requirement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "创建需求请求")
@Data
public class CreateRequirementRequest {
    /** 需求描述 */
    @NotBlank private String description;
    /** 备注 */
    private String notes;
    /** 关联系统 */
    private String system;
    /** 项目ID */
    private Long projectId;
    /** 项目类型 */
    private String projectType;
    /** 负责人ID */
    private Long personId;
    /** 负责人姓名 */
    private String personName;
    /** 优先级 */
    private String priority;
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
    /** 关联迭代ID */
    private String iterationId;
    /** 需求序号 */
    private String number;
    /** 计划完成时间 */
    private String plannedCompletionTime;
    /** 业务状态 */
    private String businessStatus;
}
