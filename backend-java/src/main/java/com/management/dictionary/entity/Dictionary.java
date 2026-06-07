package com.management.dictionary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "字典")
@Data
@TableName("dictionaries")
public class Dictionary {
    /** 字典ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典类型 */
    private String dictType;
    /** 字典键 */
    private String dictKey;
    /** 字典值 */
    private String dictValue;
    /** 排序序号 */
    private Integer sortOrder;
}
