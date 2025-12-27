package com.sc.mall.controller;

import com.sc.mall.common.Result;
import com.sc.mall.entity.User;
import com.sc.mall.entity.dto.UserLoginDTO;
import com.sc.mall.entity.dto.UserRegisterDTO;
import com.sc.mall.entity.vo.UserLoginVO;
import com.sc.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1. 认证模块", description = "用户注册与登录")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private IUserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    // @Valid 开启参数校验 (技术要求 f)
    public Result<User> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        User user = userService.register(registerDTO);
        return Result.success(user);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        UserLoginVO vo = userService.login(loginDTO);
        return Result.success(vo);
    }
}