package com.sc.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("tb_order_address")
@Schema(name = "OrderAddress", description = "订单收货地址表")
public class OrderAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID（关联 tb_order.id）")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "收货人姓名")
    @TableField("receiver_name")
    private String receiverName;

    @Schema(description = "收货人电话")
    @TableField("receiver_phone")
    private String receiverPhone;

    @Schema(description = "收货详细地址")
    @TableField("address")
    private String address;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
