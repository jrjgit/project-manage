package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.project.entity.Project;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tasks")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long projectId;
    @TableField(exist = false)
    private Project project;
    private Long creatorId;
    @TableField(exist = false)
    private User creator;
    private Long assigneeId;
    @TableField(exist = false)
    private User assignee;
    private Long devLeadId;
    @TableField(exist = false)
    private User devLead;
    private Long testerLeadId;
    @TableField(exist = false)
    private User testerLead;
    private Long testerId;
    @TableField(exist = false)
    private User tester;
    @TableField(exist = false)
    private List<TaskAssignee> assignees;
    private String rejectReason;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;
    private Long requirementId;
    private Long featureId;
    @TableField(exist = false)
    private String requirementName;
    @TableField(exist = false)
    private String requirementDesc;
    private String terminal;
    private Integer progress;
    private String performance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
