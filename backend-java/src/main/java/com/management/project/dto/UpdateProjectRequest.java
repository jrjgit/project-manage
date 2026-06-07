package com.management.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "更新项目请求")
@Data
public class UpdateProjectRequest {
    /** 项目名称 */
    private String name;
    /** 项目编号 */
    private String code;
    /** 项目类型 */
    private String projectType;
    /** 系统范围 */
    private List<Long> systemScope;
    /** 人员范围 */
    private List<Long> hrScope;
    /** 项目经理ID */
    private Long pmId;
}
