package com.taskmanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskmanage.dto.PageResult;
import com.taskmanage.dto.TaskExportDTO;
import com.taskmanage.dto.TaskQueryRequest;
import com.taskmanage.dto.TaskRequest;
import com.taskmanage.dto.TaskStatisticsDTO;
import com.taskmanage.entity.Task;
import com.taskmanage.entity.User;
import com.taskmanage.mapper.TaskMapper;
import com.taskmanage.mapper.UserMapper;
import com.taskmanage.model.enums.TaskPriorityEnum;
import com.taskmanage.model.enums.TaskStatusEnum;
import com.taskmanage.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<Task> list(TaskQueryRequest query, Long currentUserId, String role) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        if (!"导师".equals(role)) {
            query.setAssigneeId(currentUserId);
        }

        if (query.getAssigneeId() != null) {
            wrapper.eq(Task::getAssigneeId, query.getAssigneeId());
        }
        if (query.getCreatorId() != null) {
            wrapper.eq(Task::getCreatorId, query.getCreatorId());
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            wrapper.eq(Task::getStatus, query.getStatus());
        }
        if (query.getDeadlineStart() != null) {
            wrapper.ge(Task::getDeadline, query.getDeadlineStart());
        }
        if (query.getDeadlineEnd() != null) {
            wrapper.le(Task::getDeadline, query.getDeadlineEnd());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(Task::getTitle, query.getKeyword());
        }

        wrapper.orderByDesc(Task::getCreatedTime);
        Page<Task> page = new Page<>(query.getPage(), query.getPageSize());
        Page<Task> result = taskMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public Task getById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        return task;
    }

    @Override
    public void create(TaskRequest request, Long creatorId) {
        if (request.getAssigneeId() != null) {
            User assignee = userMapper.selectById(request.getAssigneeId());
            if (assignee == null) {
                throw new RuntimeException("负责人不存在");
            }
            if (!"实习生".equals(assignee.getRole())) {
                throw new RuntimeException("只能将任务分配给实习生");
            }
        }
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatusEnum.TODO.getValue());
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriorityEnum.MEDIUM.getValue());
        task.setAssigneeId(request.getAssigneeId());
        task.setCreatorId(creatorId);
        task.setDeadline(request.getDeadline());
        taskMapper.insert(task);
    }

    @Override
    public void update(Long id, TaskRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        task.setDeadline(request.getDeadline());
        taskMapper.updateById(task);
    }

    @Override
    public void delete(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        taskMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, String status) {
        if (!TaskStatusEnum.isValid(status)) {
            throw new RuntimeException("无效的状态值：" + status + "，仅允许：" + String.join("/", TaskStatusEnum.allValues()));
        }
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        task.setStatus(status);
        taskMapper.updateById(task);
    }

    @Override
    public TaskStatisticsDTO statistics(Long currentUserId, String role) {
        LambdaQueryWrapper<Task> baseWrapper = new LambdaQueryWrapper<>();
        if ("导师".equals(role)) {
            baseWrapper.eq(Task::getCreatorId, currentUserId);
        } else {
            baseWrapper.eq(Task::getAssigneeId, currentUserId);
        }

        List<Task> allTasks = taskMapper.selectList(baseWrapper);
        long total = allTasks.size();

        TaskStatisticsDTO dto = new TaskStatisticsDTO();
        dto.setTotal(total);

        for (Task t : allTasks) {
            switch (t.getStatus()) {
                case "待办" -> dto.setTodoCount(dto.getTodoCount() + 1);
                case "进行中" -> dto.setInProgressCount(dto.getInProgressCount() + 1);
                case "已完成" -> dto.setDoneCount(dto.getDoneCount() + 1);
            }
            switch (t.getPriority()) {
                case "高" -> dto.setHighCount(dto.getHighCount() + 1);
                case "中" -> dto.setMediumCount(dto.getMediumCount() + 1);
                case "低" -> dto.setLowCount(dto.getLowCount() + 1);
            }
            if (t.getDeadline() != null && t.getDeadline().isBefore(LocalDate.now())
                    && !"已完成".equals(t.getStatus())) {
                dto.setOverdueCount(dto.getOverdueCount() + 1);
            }
        }

        dto.setCompletionRate(total > 0 ? Math.round((double) dto.getDoneCount() / total * 100.0) : 0);
        return dto;
    }

    @Override
    public List<TaskExportDTO> exportList(TaskQueryRequest query, Long currentUserId, String role) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        if (!"导师".equals(role)) {
            query.setAssigneeId(currentUserId);
        }
        if (query.getAssigneeId() != null) {
            wrapper.eq(Task::getAssigneeId, query.getAssigneeId());
        }
        if (query.getCreatorId() != null) {
            wrapper.eq(Task::getCreatorId, query.getCreatorId());
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            wrapper.eq(Task::getStatus, query.getStatus());
        }
        if (query.getDeadlineStart() != null) {
            wrapper.ge(Task::getDeadline, query.getDeadlineStart());
        }
        if (query.getDeadlineEnd() != null) {
            wrapper.le(Task::getDeadline, query.getDeadlineEnd());
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(Task::getTitle, query.getKeyword());
        }
        wrapper.orderByDesc(Task::getCreatedTime);

        return taskMapper.selectList(wrapper).stream().map(t -> {
            TaskExportDTO d = new TaskExportDTO();
            d.setTitle(t.getTitle());
            d.setStatus(t.getStatus());
            d.setPriority(t.getPriority());
            d.setAssigneeId(t.getAssigneeId() != null ? String.valueOf(t.getAssigneeId()) : "");
            d.setDeadline(t.getDeadline());
            d.setCreatedTime(t.getCreatedTime());
            return d;
        }).collect(Collectors.toList());
    }
}
