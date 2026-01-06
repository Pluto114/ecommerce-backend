package com.sc.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.mall.common.Result;
import com.sc.mall.entity.dto.OrderRefundAuditDTO;
import com.sc.mall.entity.dto.OrderShipDTO;
import com.sc.mall.entity.vo.OrderVO;
import com.sc.mall.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-订单管理", description = "信息管理员/商家：订单列表、发货、退单审核")
@RestController
@RequestMapping("/merchant/order")
public class MerchantOrderController {

    @Resource
    private IOrderService orderService;

    @Operation(summary = "商家订单列表（仅本人店铺订单，可筛选状态/订单号）")
    @GetMapping("/list")
    public Result<Page<OrderVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderSn
    ) {
        Page<OrderVO> page = orderService.merchantOrderList(pageNum, pageSize, status, orderSn);
        return Result.success(page);
    }

    @Operation(summary = "发货 (1->2)")
    @PostMapping("/ship")
    public Result<String> ship(@RequestBody @Valid OrderShipDTO dto) {
        // ✅ 数据权限由 Service 层校验：order -> items -> product.shopId -> shop.adminId == 当前用户
        orderService.shipOrder(dto.getOrderSn());
        return Result.success("发货成功");
    }

    @Operation(summary = "审核退单 (-2->-3/-4)")
    @PostMapping("/refund/audit")
    public Result<String> auditRefund(@RequestBody @Valid OrderRefundAuditDTO dto) {
        // ✅ 数据权限由 Service 层校验
        orderService.auditRefund(dto.getOrderSn(), Boolean.TRUE.equals(dto.getApprove()), dto.getAdminReason());
        return Result.success("审核完成");
    }
}
