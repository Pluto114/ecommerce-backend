package com.sc.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("tb_category")
@Schema(name = "Category", description = "商品分类")
public class Category implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    @Schema(description = "1启用 0禁用")
    private Byte status;

    private Integer sort;

    @Schema(description = "父分类ID(一级为0)")
    private Long parentId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
