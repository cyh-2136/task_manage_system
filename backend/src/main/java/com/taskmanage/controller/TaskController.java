package com.taskmanage.controller;

import com.alibaba.excel.EasyExcel;
import com.taskmanage.annotation.RequireRole;
import com.taskmanage.common.Result;
import com.taskmanage.dto.PageResult;
import com.taskmanage.dto.TaskExportDTO;
import com.taskmanage.dto.TaskQueryRequest;
import com.taskmanage.dto.TaskRequest;
import com.taskmanage.dto.TaskStatisticsDTO;
import com.taskmanage.entity.Task;
import com.taskmanage.model.enums.TaskStatusEnum;
import com.taskmanage.service.TaskService;
import com.taskmanage.validation.EnumValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "任务管理")
@Validated
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "获取任务列表（支持筛选/搜索）")
    @RequireRole({"导师", "实习生"})
    @GetMapping
    public Result<PageResult<Task>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineEnd,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        TaskQueryRequest query = new TaskQueryRequest();
        query.setStatus(status);
        query.setAssigneeId(assigneeId);
        query.setCreatorId(creatorId);
        query.setDeadlineStart(deadlineStart);
        query.setDeadlineEnd(deadlineEnd);
        query.setKeyword(keyword);
        query.setPage(page);
        query.setPageSize(pageSize);
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(taskService.list(query, userId, role));
    }

    @Operation(summary = "获取任务详情")
    @RequireRole({"导师", "实习生"})
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @Operation(summary = "创建任务")
    @RequireRole({"导师"})
    @PostMapping
    public Result<Void> create(@Valid @RequestBody TaskRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        taskService.create(request, userId);
        return Result.success();
    }

    @Operation(summary = "更新任务")
    @RequireRole({"导师"})
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        taskService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除任务")
    @RequireRole({"导师"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }

    @Operation(summary = "更新任务状态")
    @RequireRole({"导师"})
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam @EnumValue(enumClass = TaskStatusEnum.class, message = "状态值仅允许：待办、进行中、已完成") String status) {
        taskService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取任务统计数据")
    @RequireRole({"导师", "实习生"})
    @GetMapping("/statistics")
    public Result<TaskStatisticsDTO> statistics(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return Result.success(taskService.statistics(userId, role));
    }

    @Operation(summary = "导出任务列表为Excel")
    @RequireRole({"导师", "实习生"})
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineEnd,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        TaskQueryRequest query = new TaskQueryRequest();
        query.setStatus(status);
        query.setAssigneeId(assigneeId);
        query.setCreatorId(creatorId);
        query.setDeadlineStart(deadlineStart);
        query.setDeadlineEnd(deadlineEnd);
        query.setKeyword(keyword);
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        List<TaskExportDTO> list = taskService.exportList(query, userId, role);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("任务列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), TaskExportDTO.class)
                .sheet("任务列表")
                .doWrite(list);
    }
}
