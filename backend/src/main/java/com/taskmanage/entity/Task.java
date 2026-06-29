package com.taskmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String description;

    private String status;

    private String priority;

    private Long assigneeId;

    private Long creatorId;

    private LocalDate deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
