package com.management.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "更新Bug请求")
@Data
public class UpdateBugRequest {
    /** Bug标题 */
    private String title;
    /** Bug描述 */
    private String description;
    /** 严重程度 */
    private String severity;
    /** 关联任务ID */
    private Long taskId;
    /** 指派人ID */
    private Long assigneeId;
    /** 备注说明 */
    private String remark;
    /** 测试类型 */
    private String testType;
}
