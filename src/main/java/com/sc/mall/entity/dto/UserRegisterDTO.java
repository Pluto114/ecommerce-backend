package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "用户注册参数")
public class UserRegisterDTO {

    @Schema(description = "用户名", example = "sicong")
    @NotBlank(message = "用户名不能为空")
    @Length(min = 4, max = 20, message = "用户名长度需在4-20位之间")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    @Schema(description = "角色: 2-信息管理员, 3-前端用户", example = "3")
    // 这里我们不做强制校验，默认为3，或者由前端传
    private Integer role;
}