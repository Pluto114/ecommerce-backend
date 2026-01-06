package com.sc.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Getter
@Setter
@TableName("tb_order")
@Schema(name = "Order", description = "订单表")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单编号 (业务唯一标识)")
    @TableField("order_sn")
    private String orderSn;

    @Schema(description = "下单用户ID (关联 tb_user.id)")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "订单总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "实际支付金额 (可能使用积分)")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @Schema(description = "支付使用积分")
    @TableField("points_used")
    private Integer pointsUsed;

    @Schema(description = "订单状态: 0-待支付, 1-待发货, 2-待收货, 3-待评价, 4-已评价, -1-取消, -2-申请退单, -3-退单成功, -4-管理员退单")
    @TableField("status")
    private Byte status;

    @Schema(description = "(-1) 取消理由")
    @TableField("cancel_reason")
    private String cancelReason;

    @Schema(description = "(-2) 退单理由")
    @TableField("refund_reason")
    private String refundReason;

    @Schema(description = "(-4) 管理员退单理由")
    @TableField("refund_admin_reason")
    private String refundAdminReason;

    @Schema(description = "下单时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "支付时间")
    @TableField("pay_time")
    private LocalDateTime payTime;

    @Schema(description = "发货时间")
    @TableField("shipping_time")
    private LocalDateTime shippingTime;

    @Schema(description = "收货时间")
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @Schema(description = "评价时间")
    @TableField("comment_time")
    private LocalDateTime commentTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "店铺ID (关联 tb_shop.id)")
    @TableField("shop_id")
    private Long shopId;

}
