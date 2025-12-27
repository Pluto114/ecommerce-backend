package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建订单参数")
public class OrderCreateDTO {

    @Schema(description = "购物车记录ID列表 (支持批量结算)", example = "[1, 2]")
    @NotEmpty(message = "请选择要结算的商品")
    private List<Long> cartIds;

    @Schema(description = "收货人姓名", example = "斯聪")
    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @Schema(description = "收货人电话", example = "13800138000")
    @NotBlank(message = "联系电话不能为空")
    private String receiverPhone;

    @Schema(description = "收货详细地址", example = "北京市海淀区清华科技园")
    @NotBlank(message = "收货地址不能为空")
    private String address;
}