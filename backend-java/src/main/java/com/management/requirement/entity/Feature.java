package com.management.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("features")
public class Feature {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long requirementId;
    private String title;
    private String description;
    private String status;
    private Long developerId;
    @TableField(exist = false)
    private User developer;
    private Long testerId;
    @TableField(exist = false)
    private User tester;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
