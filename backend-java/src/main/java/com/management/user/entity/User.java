package com.management.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户")
@Data
@TableName("users")
public class User {
    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 姓名 */
    private String name;
    /** 登录账号 */
    private String account;
    /** 登录密码（加密存储，JSON序列化时忽略） */
    @JsonIgnore
    private String password;
    /** 角色（pm=项目经理, dev_lead=开发组长, dev=开发, tester_lead=测试组长, tester=测试） */
    private String role;
    /** 邮箱 */
    private String email;
    /** 技能标签（逗号分隔） */
    private String skills;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
