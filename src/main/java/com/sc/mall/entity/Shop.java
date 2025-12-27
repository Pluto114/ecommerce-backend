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
 * 商店信息表
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Getter
@Setter
@TableName("tb_shop")
@Schema(name = "Shop", description = "商店信息表")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商店ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "隶属的信息管理员ID (关联 tb_user.id)")
    @TableField("admin_id")
    private Long adminId;

    @Schema(description = "商店名称")
    @TableField("name")
    private String name;

    @Schema(description = "商店具体地址")
    @TableField("address")
    private String address;

    @Schema(description = "商店Logo图片URL - [加分项]")
    @TableField("logo_url")
    private String logoUrl;

    @Schema(description = "商店状态: 0-关闭/禁用, 1-营业/启用")
    @TableField("status")
    private Byte status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
