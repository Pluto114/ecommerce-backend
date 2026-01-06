// OrderRefundAuditDTO.java
package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRefundAuditDTO {
    @Schema(description="订单号")
    @NotBlank
    private String orderSn;

    @Schema(description="是否同意退单：true同意->-4，false拒绝->-3")
    @NotNull
    private Boolean approve;

    @Schema(description="管理员审核意见/原因")
    @NotBlank
    private String adminReason;
}
