package com.management.requirement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.requirement.entity.Requirement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequirementMapper extends BaseMapper<Requirement> {
}
