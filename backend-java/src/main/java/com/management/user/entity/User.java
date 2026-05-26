package com.management.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String account;
    @JsonIgnore
    private String password;
    private String role;
    private Long groupId;
    private String wechatId;
    private String dingtalkId;
    private String feishuId;
    private String email;
    private LocalDateTime createdAt;
}
