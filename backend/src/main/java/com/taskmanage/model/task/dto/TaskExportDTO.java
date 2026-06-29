package com.taskmanage.model.task.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskExportDTO {

    @ExcelProperty("任务标题")
    private String title;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("优先级")
    private String priority;

    @ExcelProperty("负责人ID")
    private String assigneeId;

    @ExcelProperty("截止日期")
    private LocalDate deadline;

    @ExcelProperty("创建时间")
    private LocalDateTime createdTime;
}
