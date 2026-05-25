package com.management.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feature_assignments")
public class FeatureAssignment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long featureId;
    private String terminal;
    private Long developerId;
    @TableField(exist = false)
    private User developer;
    private String status;
    private Long autoTaskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
