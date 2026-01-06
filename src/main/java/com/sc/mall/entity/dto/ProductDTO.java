package com.sc.mall.entity.dto;

import com.sc.mall.entity.dto.group.AddGroup;
import com.sc.mall.entity.dto.group.UpdateGroup;
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
    @NotNull(message = "商品ID不能为空", groups = UpdateGroup.class)
    private Long id;

    /**
     * ✅ 多店铺改造：归属店铺由请求头 X-Shop-Id 决定
     * 因此发布时不再要求 body 里传 shopId
     * （可保留字段，便于某些场景复用/展示，但不做 AddGroup 必填校验）
     */
    @Schema(description = "归属商店ID（由请求头 X-Shop-Id 决定）", example = "10")
    private Long shopId;

    @Schema(description = "分类ID (发布时必填，可修改)", example = "2")
    @NotNull(message = "必须选择商品分类", groups = AddGroup.class)
    private Long categoryId;

    @Schema(description = "商品名称", example = "IPhone 15 Pro")
    @NotBlank(message = "商品名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private String name;

    @Schema(description = "商品描述", example = "钛金属边框，A17芯片")
    private String description;

    @Schema(description = "价格", example = "9999.00")
    @NotNull(message = "价格不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @DecimalMin(value = "0.01", message = "价格不能低于0.01", groups = {AddGroup.class, UpdateGroup.class})
    private BigDecimal price;

    @Schema(description = "库存", example = "100")
    @NotNull(message = "库存不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Min(value = 0, message = "库存不能为负数", groups = {AddGroup.class, UpdateGroup.class})
    private Integer stock;

    @Schema(description = "商品主图URL", example = "https://example.com/img.jpg")
    private String mainImageUrl;

    @Schema(description = "3D模型地址 (.glb)", example = "https://example.com/model.glb")
    private String modelUrl;

    @Schema(description = "状态: 0-下架, 1-在售", example = "1")
    private Integer status;
}
