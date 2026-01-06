package com.sc.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "轮播图返回对象")
public class BannerVO {
    private Long id;
    private String imgUrl;
    private String linkUrl; // 目前不做链接，统一返回 ""
}
