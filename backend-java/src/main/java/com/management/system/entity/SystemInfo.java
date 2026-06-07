package com.management.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "系统信息")
@Data
@TableName("systems")
public class SystemInfo {
    /** 系统ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 系统名称 */
    private String name;
    /** IT负责人 */
    private String itContact;
    /** 业务负责人 */
    private String bizContact;
    /** 技术负责人 */
    private String techContact;
    /** 系统地址 */
    private String address;
    /** 创建人用户ID */
    private Long createdBy;
    /** 创建人关联对象（非数据库字段） */
    @TableField(exist = false)
    private User creator;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
