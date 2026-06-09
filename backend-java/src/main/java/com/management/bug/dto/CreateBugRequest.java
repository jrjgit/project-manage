package com.management.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "创建Bug请求")
@Data
public class CreateBugRequest {
    /** Bug标题 */
    @NotBlank private String title;
    /** Bug描述 */
    private String description;
    /** 严重程度 */
    private String severity;
    /** 关联任务ID */
    private Long taskId;
    /** 指派人ID */
    private Long assigneeId;
    /** 测试类型 */
    private String testType;
    private String expectedResult;
    /** 关联需求ID */
    private Long requirementId;
}
