package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "加入/修改购物车参数")
public class CartDTO {

    @Schema(description = "购物车记录ID (修改数量/删除时必填)", example = "1")
    private Long id;

    @Schema(description = "商品ID (加入时必填)", example = "1001")
    // @NotNull(message = "商品ID不能为空") // 修改数量时可能不需要传productId，所以这里不在类级别强制，在逻辑里判
    private Long productId;

    @Schema(description = "数量", example = "1")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}