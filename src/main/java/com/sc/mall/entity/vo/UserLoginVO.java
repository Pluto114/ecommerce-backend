package com.sc.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录成功返回信息")
public class UserLoginVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色")
    private Integer role;

    @Schema(description = "认证令牌")
    private String token;
}