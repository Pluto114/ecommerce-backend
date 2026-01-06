package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "轮播图发布/修改参数")
public class BannerDTO {

    @Schema(description = "ID (修改时必填)")
    private Long id;

    @Schema(description = "图片URL", example = "https://xxx/banner1.jpg")
    @NotBlank(message = "imgUrl不能为空")
    private String imgUrl;

    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;

    @Schema(description = "排序: 越小越靠前", example = "0")
    private Integer sort;
}
