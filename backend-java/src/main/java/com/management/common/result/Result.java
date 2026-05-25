package com.management.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private T data;
    private String error;

    private Result() {}

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.data = data;
        return r;
    }

    public static Result<Void> ok() {
        return new Result<>();
    }

    public static Result<Void> fail(String error) {
        Result<Void> r = new Result<>();
        r.error = error;
        return r;
    }
}
