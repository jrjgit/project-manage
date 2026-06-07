package com.management.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "项目")
@Data
@TableName("projects")
public class Project {
    /** 项目ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 项目名称 */
    private String name;
    /** 项目编号 */
    private String code;
    /** 项目类型（ops=运维项目, project=项目制项目） */
    private String projectType;
    /** 系统范围（逗号分隔的系统名称列表） */
    private String systemScope;
    /** 项目成员范围（逗号分隔的人员列表） */
    private String hrScope;
    /** 项目经理用户ID */
    private Long pmId;
    /** 项目经理关联对象（非数据库字段） */
    @TableField(exist = false)
    private User pm;
    /** 创建人用户ID */
    private Long createdBy;
    /** 创建人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User creator;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
