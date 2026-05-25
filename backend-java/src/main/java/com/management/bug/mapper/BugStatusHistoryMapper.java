package com.management.bug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.bug.entity.BugStatusHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BugStatusHistoryMapper extends BaseMapper<BugStatusHistory> {
}
