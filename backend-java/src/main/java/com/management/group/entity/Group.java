package com.management.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`groups`")
public class Group {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long pmId;
    @TableField(exist = false)
    private User pm;
    private Long devLeadId;
    @TableField(exist = false)
    private User devLead;
    private String leadRole;
    private LocalDateTime createdAt;
}
