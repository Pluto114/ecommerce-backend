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
@TableName("tb_banner")
@Schema(name = "Banner", description = "首页轮播图")
public class Banner implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("img_url")
    private String imgUrl;

    @TableField("status")
    private Byte status; // 0/1

    @TableField("sort")
    private Integer sort;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
