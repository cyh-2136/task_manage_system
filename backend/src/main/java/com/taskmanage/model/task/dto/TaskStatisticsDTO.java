package com.taskmanage.model.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "任务统计数据")
public class TaskStatisticsDTO {

    @Schema(description = "总任务数")
    private long total;

    @Schema(description = "待办数")
    private long todoCount;

    @Schema(description = "进行中数")
    private long inProgressCount;

    @Schema(description = "已完成数")
    private long doneCount;

    @Schema(description = "完成率（百分比）")
    private double completionRate;

    @Schema(description = "高优先级数")
    private long highCount;

    @Schema(description = "中优先级数")
    private long mediumCount;

    @Schema(description = "低优先级数")
    private long lowCount;

    @Schema(description = "逾期未完成数")
    private long overdueCount;
}
