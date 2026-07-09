package com.management.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一返回结果包装类
 *
 * @param <T> 数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    /** 状态码，200 表示成功 */
    private Integer code;
    /** 业务数据 */
    private T data;
    /** 错误信息 */
    private String error;

    private Result() {}

    /**
     * 成功返回，携带数据
     */
    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.data = data;
        return r;
    }

    /**
     * 成功返回，无数据
     */
    public static Result<Void> ok() {
        Result<Void> r = new Result<>();
        r.code = 200;
        return r;
    }

    /**
     * 失败返回，默认 400 状态码
     */
    public static Result<Void> fail(String error) {
        Result<Void> r = new Result<>();
        r.code = 400;
        r.error = error;
        return r;
    }

    /**
     * 失败返回，指定状态码
     */
    public static Result<Void> fail(int code, String error) {
        Result<Void> r = new Result<>();
        r.code = code;
        r.error = error;
        return r;
    }
}
