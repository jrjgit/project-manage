package com.management.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "创建项目请求")
@Data
public class CreateProjectRequest {
    /** 项目名称 */
    @NotBlank
    private String name;
    /** 项目编号 */
    private String code;
    /** 项目类型 */
    private String projectType;
    /** 系统范围 */
    private List<Long> systemScope;
    /** 人员范围 */
    private List<Long> hrScope;
}
