package com.sc.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "图形验证码返回")
public class CaptchaVO {

    @Schema(description = "验证码Key（用于后续校验）")
    private String captchaKey;

    @Schema(description = "验证码图片（data:image/svg+xml;base64,...）")
    private String captchaImg;
}
