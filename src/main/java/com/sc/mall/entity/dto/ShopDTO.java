package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "店铺创建/修改参数")
public class ShopDTO {

    @Schema(description = "店铺ID (修改时必填)", example = "1")
    private Long id;

    @Schema(description = "店铺名称", example = "我的官方旗舰店")
    @NotBlank(message = "店铺名称不能为空")
    private String name;

    @Schema(description = "店铺地址", example = "厦门市集美区理工路")
    @NotBlank(message = "店铺地址不能为空")
    private String address;

    @Schema(description = "店铺Logo图片URL", example = "https://example.com/logo.png")
    private String logoUrl;
}