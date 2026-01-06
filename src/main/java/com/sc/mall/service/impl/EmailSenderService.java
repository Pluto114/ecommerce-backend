package com.sc.mall.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailSenderService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private Environment env;

    public void sendVerifyCode(String toEmail, String code) {
        String from = env.getProperty("spring.mail.username");
        if (from == null || from.isBlank()) {
            throw new RuntimeException("邮件发送失败：未配置 spring.mail.username");
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);           // QQ 强烈建议 from 必须等于 username
        msg.setTo(toEmail);
        msg.setSubject("【商城】邮箱验证码");
        msg.setText("您的验证码是：" + code + "，5分钟内有效。如非本人操作请忽略。");

        try {
            log.info("[MAIL] 准备发送验证码: from={}, to={}", from, mask(toEmail));
            mailSender.send(msg);
            log.info("[MAIL] 发送成功: to={}", mask(toEmail));
        } catch (MailException e) {
            log.error("[MAIL] 发送失败: from={}, to={}, err={}", from, mask(toEmail), e.toString(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    private String mask(String email) {
        if (email == null) return "";
        int at = email.indexOf("@");
        if (at <= 1) return "***" + (at > 0 ? email.substring(at) : "");
        return email.charAt(0) + "***" + email.substring(at - 1);
    }
}
