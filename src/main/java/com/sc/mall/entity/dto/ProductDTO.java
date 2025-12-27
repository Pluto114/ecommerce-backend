package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品发布/修改参数")
public class ProductDTO {

    @Schema(description = "商品ID (修改时必填)", example = "1")
    private Long id;

    @Schema(description = "归属商店ID (发布时必填)", example = "10")
    @NotNull(message = "必须指定归属商店")
    private Long shopId;

    @Schema(description = "商品名称", example = "IPhone 15 Pro")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "商品描述", example = "钛金属边框，A17芯片")
    private String description;

    @Schema(description = "价格", example = "9999.00")
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格不能低于0.01")
    private BigDecimal price;

    @Schema(description = "库存", example = "100")
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Schema(description = "商品主图URL", example = "https://example.com/img.jpg")
    private String mainImageUrl;

    @Schema(description = "状态: 0-下架, 1-在售", example = "1")
    private Integer status;
}