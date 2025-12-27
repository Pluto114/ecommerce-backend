package com.sc.mall.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// 统一返回结果类
@Data
@Schema(name = "统一返回结果", description = "后端返回给前端的统一JSON格式")
public class Result<T> {

    @Schema(description = "业务状态码, 200-成功, 非200-失败")
    private Integer code; // 状态码

    @Schema(description = "提示信息")
    private String message; // 提示信息

    @Schema(description = "数据")
    private T data; // 数据

    // 私有化构造函数，防止外部 new
    private Result() {
    }

    // 成功 - 返回数据
    public static <T> Result<T> success(T object) {
        Result<T> r = new Result<>();
        r.data = object;
        r.code = 200; // 约定 200 为成功
        r.message = "操作成功";
        return r;
    }

    // 成功 - 不返回数据
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = msg;
        return r;
    }

    // 失败 - 使用默认500状态码
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500; // 约定 500 为通用错误
        r.message = msg;
        return r;
    }
}