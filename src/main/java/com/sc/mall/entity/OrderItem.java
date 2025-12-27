package com.sc.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 订单详情表 (商品快照)
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Getter
@Setter
@TableName("tb_order_item")
@Schema(name = "OrderItem", description = "订单详情表 (商品快照)")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单详情ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID (关联 tb_order.id)")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "商品ID (关联 tb_product.id)")
    @TableField("product_id")
    private Long productId;

    @Schema(description = "商品名称 (快照)")
    @TableField("product_name")
    private String productName;

    @Schema(description = "商品图片 (快照)")
    @TableField("product_image")
    private String productImage;

    @Schema(description = "商品价格 (快照)")
    @TableField("product_price")
    private BigDecimal productPrice;

    @Schema(description = "购买数量")
    @TableField("quantity")
    private Integer quantity;
}
