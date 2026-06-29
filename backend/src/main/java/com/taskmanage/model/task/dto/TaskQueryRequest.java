package com.taskmanage.model.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "任务查询参数")
public class TaskQueryRequest {

    @Schema(description = "状态：待办/进行中/已完成")
    private String status;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "截止日期（起始）")
    private LocalDate deadlineStart;

    @Schema(description = "截止日期（结束）")
    private LocalDate deadlineEnd;

    @Schema(description = "关键词搜索（标题）")
    private String keyword;

    @Schema(description = "页码（从1开始）")
    private int page = 1;

    @Schema(description = "每页条数")
    private int pageSize = 20;
}
