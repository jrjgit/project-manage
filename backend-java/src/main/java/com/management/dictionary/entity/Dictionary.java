package com.management.dictionary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dictionaries")
public class Dictionary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictType;
    private String dictKey;
    private String dictValue;
    private Integer sortOrder;
}
