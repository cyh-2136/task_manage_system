package com.taskmanage.service;

import com.taskmanage.dto.PageResult;
import com.taskmanage.dto.TaskExportDTO;
import com.taskmanage.dto.TaskQueryRequest;
import com.taskmanage.dto.TaskRequest;
import com.taskmanage.dto.TaskStatisticsDTO;
import com.taskmanage.entity.Task;

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
