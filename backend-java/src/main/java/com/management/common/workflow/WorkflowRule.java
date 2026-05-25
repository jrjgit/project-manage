package com.management.common.workflow;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("workflow_rules")
public class WorkflowRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleType;
    private String role;
    private String fromStatus;
    private String toStatus;
}
