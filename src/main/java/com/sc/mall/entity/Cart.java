package com.sc.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 购物车表
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Getter
@Setter
@TableName("tb_cart")
@Schema(name = "Cart", description = "购物车表")
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "购物车ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID (关联 tb_user.id)")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "商品ID (关联 tb_product.id)")
    @TableField("product_id")
    private Long productId;

    @Schema(description = "数量")
    @TableField("quantity")
    private Integer quantity;

    @Schema(description = "添加时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
