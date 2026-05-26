package com.management.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("projects")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String projectType;
    private String systemScope;
    private String hrScope;
    private Long pmId;
    @TableField(exist = false)
    private User pm;
    private Long createdBy;
    @TableField(exist = false)
    private User creator;
    @TableField(exist = false)
    private List<User> hrUsers;
    private LocalDateTime createdAt;
}
