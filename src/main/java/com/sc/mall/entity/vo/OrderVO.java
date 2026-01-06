package com.sc.mall.entity.vo;

import com.sc.mall.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "订单详情/列表 VO")
public class OrderVO {

    // ==================== 订单主信息 ====================

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "下单用户ID")
    private Long userId;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实际支付金额")
    private BigDecimal payAmount;

    @Schema(description = "使用积分")
    private Integer pointsUsed;

    /**
     * 与 Order.status 保持一致：
     * 0-待支付, 1-待发货, 2-待收货, 3-待评价, 4-已评价,
     * -1-取消, -2-申请退单, -3-拒绝退单, -4-退单成功
     */
    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "取消原因（-1）")
    private String cancelReason;

    @Schema(description = "退单原因（-2）")
    private String refundReason;

    @Schema(description = "商家/管理员退单处理原因（-3/-4）")
    private String refundAdminReason;

    // ==================== 时间信息 ====================

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "发货时间")
    private LocalDateTime shippingTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "评价时间")
    private LocalDateTime commentTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    // ==================== 收货信息（结构化字段，新版） ====================

    @Schema(description = "收货人姓名")
    private String receiverName;

    @Schema(description = "收货人电话")
    private String receiverPhone;

    @Schema(description = "收货详细地址")
    private String address;

    // ==================== 收货信息（兼容字段，旧版拼接展示） ====================

    @Schema(description = "收货信息拼接（兼容旧前端展示，可选）")
    private String receiverInfo;

    // ==================== 订单商品明细 ====================

    @Schema(description = "订单包含的商品列表")
    private List<OrderItem> orderItems;

    // ==================== 前端辅助字段（可选） ====================

    @Schema(description = "订单状态中文（前端可直接展示）")
    private String statusText;
}
