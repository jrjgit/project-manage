package com.management.dictionary;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.dictionary.entity.Dictionary;
import com.management.dictionary.mapper.DictionaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private final DictionaryMapper dictionaryMapper;

    public List<Dictionary> listByType(String dictType) {
        return dictionaryMapper.selectList(
                new LambdaQueryWrapper<Dictionary>()
                        .eq(Dictionary::getDictType, dictType)
                        .orderByAsc(Dictionary::getSortOrder));
    }

    public List<Dictionary> listAll() {
        return dictionaryMapper.selectList(
                new LambdaQueryWrapper<Dictionary>().orderByAsc(Dictionary::getSortOrder));
    }

    public Dictionary create(Dictionary dict) {
        dictionaryMapper.insert(dict);
        return dict;
    }

    public Dictionary update(Long id, Dictionary dict) {
        dict.setId(id);
        dictionaryMapper.updateById(dict);
        return dict;
    }

    public void delete(Long id) {
        dictionaryMapper.deleteById(id);
    }
}
