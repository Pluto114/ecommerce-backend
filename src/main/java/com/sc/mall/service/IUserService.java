package com.sc.mall.service;

import com.sc.mall.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sc.mall.entity.dto.UserLoginDTO;
import com.sc.mall.entity.dto.UserRegisterDTO;
import com.sc.mall.entity.vo.UserLoginVO;
import com.sc.mall.entity.dto.EmailRegisterDTO;
import com.sc.mall.entity.dto.EmailSendCodeDTO;
import com.sc.mall.entity.vo.CaptchaVO;
/**
 * <p>
 * 用户表 (三合一) 服务类
 * </p>
 *
 * @author 斯聪
 * @since 2025-05-29
 */
public interface IUserService extends IService<User> {

    /**
     * 用户注册
     * @param registerDTO 注册参数
     * @return 注册成功的用户信息
     */
    User register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return 登录成功信息（含Token）
     */
    UserLoginVO login(UserLoginDTO loginDTO);

    // 新增的：
    CaptchaVO createCaptcha();
    void sendEmailCode(EmailSendCodeDTO dto, String clientIp);
    User registerByEmail(EmailRegisterDTO dto);
}