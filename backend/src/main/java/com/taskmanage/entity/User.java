package com.taskmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String role;

    private String avatar;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
