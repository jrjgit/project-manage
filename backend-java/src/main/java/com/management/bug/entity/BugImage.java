package com.management.bug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Bug截图")
@Data
@TableName("bug_images")
public class BugImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bugId;
    private String imagePath;
    private String imageName;
    private Long imageSize;
    private LocalDateTime createdAt;
}
