// OrderShipDTO.java
package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderShipDTO {
    @Schema(description="订单号")
    @NotBlank
    private String orderSn;
}
