package com.management.common.workflow;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "工作流规则")
@Data
@TableName("workflow_rules")
public class WorkflowRule {
    /** 规则ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 规则类型（task/bug/requirement） */
    private String ruleType;
    /** 适用角色 */
    private String role;
    /** 起始状态 */
    private String fromStatus;
    /** 目标状态 */
    private String toStatus;
}
