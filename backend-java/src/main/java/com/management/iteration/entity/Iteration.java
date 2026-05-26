package com.management.iteration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@TableName("iterations")
public class Iteration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String iterationId;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime releaseTime;
    private String notes;
    private String releaseNotes;
    private Long createdBy;
    @TableField(exist = false)
    private User creator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
