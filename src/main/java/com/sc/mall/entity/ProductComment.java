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
 * 商品评价表
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Getter
@Setter
@TableName("tb_product_comment")
@Schema(name = "ProductComment", description = "商品评价表")
public class ProductComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "评价ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单详情ID (关联 tb_order_item.id)")
    @TableField("order_item_id")
    private Long orderItemId;

    @Schema(description = "用户ID (关联 tb_user.id)")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "商品ID (关联 tb_product.id)")
    @TableField("product_id")
    private Long productId;

    @Schema(description = "评分 (1-5星)")
    @TableField("rating")
    private Byte rating;

    @Schema(description = "评价内容")
    @TableField("comment")
    private String comment;

    @Schema(description = "评价时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
