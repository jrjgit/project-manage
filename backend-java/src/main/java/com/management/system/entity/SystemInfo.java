package com.management.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("systems")
public class SystemInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String itContact;
    private String bizContact;
    private String techContact;
    private String address;
    private Long createdBy;
    @TableField(exist = false)
    private User creator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
