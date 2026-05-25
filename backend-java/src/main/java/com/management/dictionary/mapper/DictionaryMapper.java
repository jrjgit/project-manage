package com.management.dictionary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.dictionary.entity.Dictionary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictionaryMapper extends BaseMapper<Dictionary> {
}
