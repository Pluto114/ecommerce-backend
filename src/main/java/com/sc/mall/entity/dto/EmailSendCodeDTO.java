package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "发送邮箱验证码参数")
public class EmailSendCodeDTO {

    @Schema(description = "邮箱", example = "xxx@outlook.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "图形验证码Key", example = "b9e1d2...")
    @NotBlank(message = "captchaKey不能为空")
    private String captchaKey;

    @Schema(description = "图形验证码Code", example = "aB3d")
    @NotBlank(message = "captchaCode不能为空")
    private String captchaCode;
}
