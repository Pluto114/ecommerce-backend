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
 * 商品信息表
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Getter
@Setter
@TableName("tb_product")
@Schema(name = "Product", description = "商品信息表")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "隶属的商店ID (关联 tb_shop.id)")
    @TableField("shop_id")
    private Long shopId;

    @Schema(description = "商品名称")
    @TableField("name")
    private String name;

    @Schema(description = "商品描述")
    @TableField("description")
    private String description;

    @Schema(description = "商品价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "商品主图URL - [加分项]")
    @TableField("main_image_url")
    private String mainImageUrl;

    @Schema(description = "商品库存")
    @TableField("stock")
    private Integer stock;

    @Schema(description = "商品状态: 0-下架, 1-在售")
    @TableField("status")
    private Byte status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
