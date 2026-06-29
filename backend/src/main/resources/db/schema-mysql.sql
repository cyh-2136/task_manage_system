-- ============================================================
-- 内部任务管理系统 - MySQL 建表脚本
-- 使用前请先创建数据库：CREATE DATABASE task_manage DEFAULT CHARSET utf8mb4;
-- ============================================================

CREATE DATABASE IF NOT EXISTS task_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE task_manage;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT        PRIMARY KEY COMMENT '用户ID（雪花ID）',
    username    VARCHAR(50)  NOT NULL UNIQUE              COMMENT '用户名',
    password    VARCHAR(255) NOT NULL                      COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(50)                                 COMMENT '昵称',
    role        VARCHAR(20)  NOT NULL DEFAULT '实习生'    COMMENT '角色：导师 / 实习生',
    avatar      VARCHAR(255)                                COMMENT '头像URL',
    created_time DATETIME   DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    updated_time DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT(1)  DEFAULT 0                     COMMENT '逻辑删除标记(0-未删,1-已删)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    id          BIGINT        PRIMARY KEY COMMENT '任务ID（雪花ID）',
    title       VARCHAR(200) NOT NULL                      COMMENT '任务标题',
    description TEXT                                        COMMENT '任务描述',
    status      VARCHAR(20)  NOT NULL DEFAULT '待办'      COMMENT '状态：待办 / 进行中 / 已完成',
    priority    VARCHAR(10)  NOT NULL DEFAULT '中'         COMMENT '优先级：高 / 中 / 低',
    assignee_id BIGINT                                     COMMENT '负责人ID（关联sys_user.id）',
    creator_id  BIGINT       NOT NULL                      COMMENT '创建人ID（关联sys_user.id）',
    deadline    DATE                                        COMMENT '截止日期',
    created_time DATETIME   DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
    updated_time DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT(1)  DEFAULT 0                     COMMENT '逻辑删除标记(0-未删,1-已删)',
    INDEX idx_assignee (assignee_id),
    INDEX idx_creator  (creator_id),
    INDEX idx_status   (status),
    INDEX idx_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';
