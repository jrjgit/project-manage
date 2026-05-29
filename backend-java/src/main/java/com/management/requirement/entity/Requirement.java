package com.management.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.project.entity.Project;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("requirements")
public class Requirement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requirementId;
    private String number;
    private String description;
    private String notes;
    private String status;
    private String priority;
    @TableField("`system`")
    private String system;
    private Long projectId;
    @TableField(exist = false)
    private Project project;
    private String projectType;
    private Long personId;
    @TableField(exist = false)
    private User person;
    private String personName;
    private String totalAmount;
    private String totalPrice;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private LocalDateTime releaseTime;
    private LocalDateTime plannedCompletionTime;
    private Long devLeadId;
    @TableField(exist = false)
    private User devLead;
    private String iterationId;
    private String documentPath;
    private String documentName;
    private Long documentSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String iterationName;
    @TableField(exist = false)
    private Integer devProgress;
    @TableField(exist = false)
    private Integer integrationTestProgress;
    @TableField(exist = false)
    private Integer businessTestProgress;
    @TableField(exist = false)
    private Integer itTestProgress;
}
