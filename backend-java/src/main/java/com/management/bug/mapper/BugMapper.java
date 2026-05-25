package com.management.bug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.bug.entity.Bug;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BugMapper extends BaseMapper<Bug> {
}
