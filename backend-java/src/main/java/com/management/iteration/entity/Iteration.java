package com.management.iteration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Schema(description = "迭代")
@Data
@TableName("iterations")
public class Iteration {
    /** 迭代主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 迭代名称 */
    private String name;
    /** 迭代状态（pending_publish=待发布, published=已发布） */
    private String status;
    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime releaseTime;
    /** 备注信息 */
    private String notes;
    /** 发布说明 */
    private String releaseNotes;
    /** 创建人用户ID */
    private Long createdBy;
    /** 创建人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User creator;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
