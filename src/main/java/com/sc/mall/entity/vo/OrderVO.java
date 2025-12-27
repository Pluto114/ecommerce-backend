package com.sc.mall.entity.vo;

import com.sc.mall.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "订单详情VO")
public class OrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态: 0-待支付, 1-待发货, 2-待收货, 3-已完成, -1-已取消")
    private Integer status;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "收货信息拼接")
    private String receiverInfo;

    @Schema(description = "订单包含的商品列表")
    private List<OrderItem> orderItems;
}