package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "商店创建/修改参数")
public class ShopDTO {

    @Schema(description = "商店ID (修改时必填)", example = "1")
    private Long id;

    @Schema(description = "商店名称", example = "斯聪的旗舰店")
    @NotBlank(message = "商店名称不能为空")
    private String name;

    @Schema(description = "商店地址", example = "北京市海淀区xx路")
    @NotBlank(message = "商店地址不能为空")
    private String address;

    @Schema(description = "商店Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;
}