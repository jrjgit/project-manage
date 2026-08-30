package com.management.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 强制覆盖：updateById 传查出的实体时 updatedAt 非 null，strictUpdateFill 不填充，
        // 会 SET updated_at = 旧值，时间戳停在创建时刻，绩效归属周期随之失真
        if (metaObject.hasSetter("updatedAt")) {
            this.setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
        }
    }
}
