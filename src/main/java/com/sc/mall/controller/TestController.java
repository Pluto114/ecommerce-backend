package com.sc.mall.controller;

import com.sc.mall.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "0. 测试模块", description = "用于测试框架是否搭建成功")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "测试 Result.success")
    @GetMapping("/success")
    public Result<String> testSuccess() {
        return Result.success("斯聪，你的 Result 格式工作正常！");
    }

    @Operation(summary = "测试 全局异常处理")
    @GetMapping("/error")
    public Result<String> testError() {
        // 故意制造一个空指针异常
        String s = null;
        s.length();

        // 这行代码永远不会执行，因为上面已经抛出异常
        return Result.error("测试失败");
    }
}