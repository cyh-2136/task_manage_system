package com.taskmanage.service;

import com.taskmanage.model.common.dto.PageResult;
import com.taskmanage.model.task.dto.TaskExportDTO;
import com.taskmanage.model.task.dto.TaskQueryRequest;
import com.taskmanage.model.task.dto.TaskRequest;
import com.taskmanage.model.task.dto.TaskStatisticsDTO;
import com.taskmanage.model.task.entity.Task;

import java.util.List;

public interface TaskService {
    PageResult<Task> list(TaskQueryRequest query, Long currentUserId, String role);

    Task getById(Long id);

    void create(TaskRequest request, Long creatorId);

    void update(Long id, TaskRequest request);

    void delete(Long id);

    void updateStatus(Long id, String status);

    TaskStatisticsDTO statistics(Long currentUserId, String role);

    List<TaskExportDTO> exportList(TaskQueryRequest query, Long currentUserId, String role);
}
