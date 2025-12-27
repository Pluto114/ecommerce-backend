package com.sc.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.mall.common.Result;
import com.sc.mall.entity.dto.OrderCreateDTO;
import com.sc.mall.entity.vo.OrderVO;
import com.sc.mall.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "6. 订单模块", description = "核心业务：下单/支付/列表")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private IOrderService orderService;

    @Operation(summary = "创建订单 (购物车结算)")
    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody @Valid OrderCreateDTO createDTO) {
        String orderSn = orderService.createOrder(createDTO);
        // 修正：Result类只定义了 success(T data)，没有 success(msg, data)
        // 默认 message 为 "操作成功"，data 为 orderSn
        return Result.success(orderSn);
    }

    @Operation(summary = "模拟支付订单")
    @PostMapping("/pay/{orderSn}")
    public Result<String> payOrder(@PathVariable String orderSn) {
        orderService.payOrder(orderSn);
        // 这里 data 返回 "支付成功" 字符串，前端可以取 data 显示
        return Result.success("支付成功");
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/my-list")
    public Result<Page<OrderVO>> myList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        Page<OrderVO> page = orderService.myOrderList(pageNum, pageSize, status);
        return Result.success(page);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel/{orderSn}")
    public Result<String> cancelOrder(@PathVariable String orderSn) {
        orderService.cancelOrder(orderSn);
        return Result.success("订单已取消");
    }
}