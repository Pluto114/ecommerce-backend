package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "邮箱注册参数（邮箱作为登录账号）")
public class EmailRegisterDTO {

    @Schema(description = "邮箱（登录账号）", example = "xxx@outlook.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "邮箱验证码不能为空")
    private String emailCode;

    @Schema(description = "密码", example = "12345678")
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    @Schema(description = "昵称（可选，不传就用邮箱前缀）", example = "sicong")
    private String username;
}
