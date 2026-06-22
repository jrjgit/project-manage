package com.management.requirement.dto;

import com.management.bug.entity.Bug;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "需求详情DTO")
@Data
public class RequirementDetailDTO {
    /** 需求主键ID */
    private Long id;
    /** 需求编号 */
    private String requirementId;
    /** 序号 */
    private String number;
    /** 描述 */
    private String description;
    /** 备注 */
    private String notes;
    /** 状态 */
    private String status;
    /** 优先级 */
    private String priority;
    /** 关联系统 */
    private String system;
    /** 项目ID */
    private Long projectId;
    /** 关联项目 */
    private Object project;
    /** 项目类型 */
    private String projectType;
    /** 负责人ID */
    private Long personId;
    /** 负责人 */
    private Object person;
    /** 负责人姓名 */
    private String personName;
    /** 总人天 */
    private String totalAmount;
    /** 总金额 */
    private String totalPrice;
    /** 开发人天 */
    private String devTotal;
    /** 开发单价 */
    private String devPrice;
    /** 测试人天 */
    private String testTotal;
    /** 测试单价 */
    private String testPrice;
    /** 上线时间 */
    private String releaseTime;
    /** 计划完成时间 */
    private String plannedCompletionTime;
    /** 开发组长ID */
    private Long devLeadId;
    /** 开发组长 */
    private Object devLead;
    /** 迭代ID */
    private String iterationId;
    /** 迭代名称 */
    private String iterationName;
    /** 文档路径 */
    private String documentPath;
    /** 文档名 */
    private String documentName;
    /** 文档大小 */
    private Long documentSize;
    /** 进度备注 */
    private String progressNotes;
    /** 创建时间 */
    private String createdAt;
    /** 更新时间 */
    private String updatedAt;

    /** 开发进度 */
    private DevProgress devProgress;
    /** 集成测试Bug列表 */
    private List<Bug> integrationTestBugs;
    /** 业务测试Bug列表 */
    private List<Bug> businessTestBugs;
    /** IT测试Bug列表 */
    private List<Bug> itTestBugs;

    @Data
    public static class DevProgress {
        /** 各终端进度列表 */
        private List<TerminalProgress> terminals;
    }

    @Data
    public static class TerminalProgress {
        /** 终端名称 */
        private String terminal;
        /** 进度百分比 */
        private int progress;
        /** 任务总数 */
        private long total;
        /** 已完成数 */
        private long done;
        /** 逾期天数 */
        private long overdueDays;
        /** 关联任务列表 */
        private List<TaskBrief> tasks;
    }

    @Data
    public static class TaskBrief {
        /** 任务ID */
        private Long id;
        /** 任务标题 */
        private String title;
        /** 任务状态 */
        private String status;
    }
}
