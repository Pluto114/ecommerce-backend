package com.sc.mall.controller;

import com.sc.mall.common.Result;
import com.sc.mall.entity.dto.OrderRefundAuditDTO;
import com.sc.mall.entity.dto.OrderShipDTO;
import com.sc.mall.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// 如果你们启用了 Spring Security 方法级鉴权，可打开这行：
// import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "后台-订单管理", description = "管理员：发货、退单审核")
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Resource
    private IOrderService orderService;

    @Operation(summary = "发货 (1->2)")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping("/ship")
    public Result<String> ship(@RequestBody @Valid OrderShipDTO dto) {
        orderService.shipOrder(dto.getOrderSn());
        return Result.success("发货成功");
    }

    @Operation(summary = "审核退单 (-2->-3/-4)")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping("/refund/audit")
    public Result<String> auditRefund(@RequestBody @Valid OrderRefundAuditDTO dto) {
        orderService.auditRefund(dto.getOrderSn(), Boolean.TRUE.equals(dto.getApprove()), dto.getAdminReason());
        return Result.success("审核完成");
    }
}
