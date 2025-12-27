package com.sc.mall.common.exception;

import com.sc.mall.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常处理器
@Slf4j
@RestControllerAdvice // 标记这个类是 SpringMVC 的全局异常处理类
public class GlobalExceptionHandler {

    /**
     * 1. 处理 @Valid 校验失败（技术要求 f）
     * 当 DTO 中的字段校验失败时，会抛出这个异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验失败: {}", e.getMessage());

        // 从异常中获取第一个校验失败的信息，返回给前端
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return Result.error(400, errorMessage); // 400 通常代表客户端参数错误
    }

    /**
     * 2. 处理所有未捕获的常规异常
     * (比如 NullPointerException, ArithmeticException 等)
     */
    @ExceptionHandler(Exception.class) // 捕获所有 Exception 类型的异常
    public Result<String> handleException(Exception e) {
        log.error("服务器发生未知异常: ", e); // 打印完整的错误堆栈

        // 返回一个通用的服务器内部错误提示
        return Result.error(500, "服务器内部错误，请联系斯聪管理员");
    }
}