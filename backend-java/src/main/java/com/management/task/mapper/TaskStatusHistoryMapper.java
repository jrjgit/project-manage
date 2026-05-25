package com.management.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.task.entity.TaskStatusHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskStatusHistoryMapper extends BaseMapper<TaskStatusHistory> {
}
