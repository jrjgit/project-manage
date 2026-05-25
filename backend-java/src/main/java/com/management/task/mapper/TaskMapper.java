package com.management.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.task.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
