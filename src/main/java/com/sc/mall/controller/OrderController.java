package com.sc.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.mall.common.Result;
import com.sc.mall.entity.dto.OrderCreateDTO;
import com.sc.mall.entity.dto.OrderRefundApplyDTO;
import com.sc.mall.entity.vo.OrderVO;
import com.sc.mall.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "6. 订单模块(用户端)", description = "用户：下单/支付/取消/收货/退单/评价/列表")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private IOrderService orderService;

    @Operation(summary = "创建订单 (购物车结算)")
    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody @Valid OrderCreateDTO createDTO) {
        String orderSn = orderService.createOrder(createDTO);
        return Result.success(orderSn);
    }

    @Operation(summary = "模拟支付订单 (0->1)")
    @PostMapping("/pay/{orderSn}")
    public Result<String> payOrder(@PathVariable String orderSn) {
        orderService.payOrder(orderSn);
        return Result.success("支付成功");
    }

    @Operation(summary = "取消订单 (0->-1)")
    @PostMapping("/cancel/{orderSn}")
    public Result<String> cancelOrder(@PathVariable String orderSn) {
        orderService.cancelOrder(orderSn);
        return Result.success("订单已取消");
    }

    @Operation(summary = "确认收货 (2->3)")
    @PostMapping("/receive/{orderSn}")
    public Result<String> receiveOrder(@PathVariable String orderSn) {
        orderService.receiveOrder(orderSn);
        return Result.success("确认收货成功");
    }

    @Operation(summary = "申请退单 (1/2->-2)")
    @PostMapping("/refund/apply")
    public Result<String> applyRefund(@RequestBody @Valid OrderRefundApplyDTO dto) {
        orderService.applyRefund(dto.getOrderSn(), dto.getRefundReason());
        return Result.success("退单申请已提交");
    }

    /**
     * 若你们评论模块是真正落库在 Comment 表里：
     * 推荐做法：评论保存成功后（同事务或后续）调用此接口推进订单状态 3->4
     */
    @Operation(summary = "评价完成（仅推进订单状态 3->4）")
    @PostMapping("/comment/finish/{orderSn}")
    public Result<String> finishComment(@PathVariable String orderSn) {
        orderService.finishComment(orderSn);
        return Result.success("订单已完成");
    }

    @Operation(summary = "我的订单列表（分页，可按状态筛选）")
    @GetMapping("/my-list")
    public Result<Page<OrderVO>> myList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        Page<OrderVO> page = orderService.myOrderList(pageNum, pageSize, status);
        return Result.success(page);
    }

//    @Operation(summary = "订单详情（用户端，仅自己订单）")
//    @GetMapping("/detail/{orderSn}")
//    public Result<OrderVO> detail(@PathVariable String orderSn) {
//        // 你可以在 service 写 getMyOrderDetail(orderSn)，或复用 myOrderList 的拼 VO 逻辑
//        OrderVO vo = orderService.myOrderDetail(orderSn);
//        return Result.success(vo);
//    }

}
