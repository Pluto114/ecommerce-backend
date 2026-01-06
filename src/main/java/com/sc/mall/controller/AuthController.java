package com.sc.mall.controller;

import com.sc.mall.common.Result;
import com.sc.mall.entity.User;
import com.sc.mall.entity.dto.EmailRegisterDTO;
import com.sc.mall.entity.dto.EmailSendCodeDTO;
import com.sc.mall.entity.dto.UserLoginDTO;
import com.sc.mall.entity.dto.UserRegisterDTO;
import com.sc.mall.entity.vo.CaptchaVO;
import com.sc.mall.entity.vo.UserLoginVO;
import com.sc.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1. 认证模块", description = "用户注册与登录")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private IUserService userService;

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        try {
            return Result.success(userService.createCaptcha());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }


    @Operation(summary = "发送邮箱验证码（先校验图形验证码）")
    @PostMapping("/email/send-code")
    public Result<String> sendEmailCode(@RequestBody @Valid EmailSendCodeDTO dto, HttpServletRequest request) {
        try {
            String ip = request.getRemoteAddr();
            userService.sendEmailCode(dto, ip);
            return Result.success("验证码已发送");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "邮箱注册（邮箱作为登录账号）")
    @PostMapping("/register/email")
    public Result<User> registerByEmail(@RequestBody @Valid EmailRegisterDTO dto) {
        try {
            User user = userService.registerByEmail(dto);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // ======== 你原有接口：保留兼容 ========

    @Operation(summary = "用户注册（旧接口，不走邮箱验证码）")
    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        try {
            User user = userService.register(registerDTO);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "用户登录（支持邮箱或用户名登录）")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        try {
            UserLoginVO vo = userService.login(loginDTO);
            return Result.success(vo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage()); // 关键：不再500
        }
    }
}
