package com.taskmanage.dto;

import com.taskmanage.model.enums.TaskPriorityEnum;
import com.taskmanage.model.enums.TaskStatusEnum;
import com.taskmanage.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "创建/更新任务请求")
public class TaskRequest {

    @NotBlank(message = "任务标题不能为空")
    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "状态：待办/进行中/已完成")
    @EnumValue(enumClass = TaskStatusEnum.class, message = "状态值仅允许：待办、进行中、已完成")
    private String status;

    @Schema(description = "优先级：高/中/低")
    @EnumValue(enumClass = TaskPriorityEnum.class, message = "优先级仅允许：高、中、低")
    private String priority;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "截止日期")
    private LocalDate deadline;
}
