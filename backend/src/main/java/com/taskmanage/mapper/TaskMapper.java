package com.taskmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskmanage.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
