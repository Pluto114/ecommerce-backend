package com.sc.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.mall.common.utils.SecurityUtils;
import com.sc.mall.entity.*;
import com.sc.mall.entity.dto.OrderCreateDTO;
import com.sc.mall.entity.vo.OrderVO;
import com.sc.mall.mapper.OrderMapper;
import com.sc.mall.service.*;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Resource
    private ICartService cartService;
    @Resource
    private IProductService productService;
    @Resource
    private IOrderItemService orderItemService;

    // 核心事务：要么全部成功，要么全部回滚 (库存不扣、钱不扣、订单不生成)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderCreateDTO createDTO) {
        Long userId = SecurityUtils.getUserId();

        // 1. 查询勾选的购物车商品
        List<Cart> cartList = cartService.listByIds(createDTO.getCartIds());
        if (cartList.isEmpty()) {
            throw new RuntimeException("请选择要结算的商品");
        }
        // 校验这些购物车记录是不是当前用户的 (防止恶意传别人的ID)
        for (Cart cart : cartList) {
            if (!cart.getUserId().equals(userId)) {
                throw new RuntimeException("非法操作：购物车信息有误");
            }
        }

        // 2. 准备数据
        List<Long> productIds = cartList.stream().map(Cart::getProductId).collect(Collectors.toList());
        List<Product> products = productService.listByIds(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 生成订单号 (时间戳 + 4位随机数)
        String orderSn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        // 3. 循环处理每个商品：校验库存、扣库存、算钱、生成快照
        for (Cart cart : cartList) {
            Product product = productMap.get(cart.getProductId());
            if (product == null) {
                throw new RuntimeException("部分商品已下架或不存在");
            }
            if (product.getStatus() == 0) {
                throw new RuntimeException("商品 [" + product.getName() + "] 已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足");
            }

            // A. 扣减库存 (乐观锁/直接SQL更新)
            // update tb_product set stock = stock - ? where id = ? and stock >= ?
            boolean updateResult = productService.update(
                    new LambdaUpdateWrapper<Product>()
                            .setSql("stock = stock - " + cart.getQuantity())
                            .eq(Product::getId, product.getId())
                            .ge(Product::getStock, cart.getQuantity()) // 双重保险：数据库层面确保不超卖
            );
            if (!updateResult) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足(并发拦截)");
            }

            // B. 累加总价
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // C. 构建订单详情 (快照)
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImageUrl());
            item.setProductPrice(product.getPrice()); // 关键：记录当时购买单价
            item.setQuantity(cart.getQuantity());
            // item.setOrderId(...) 等保存order拿到ID后再填
            orderItems.add(item);
        }

        // 4. 保存主订单
        Order order = new Order();
        order.setOrderSn(orderSn);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus((byte) 0); // 0-待支付
        // 这里简单拼接一下地址存到备注或扩展字段里，实际项目有专门的 Address 表
        // 我们在数据库设计时没留 address 字段，暂时不存或存到 cancel_reason 借用一下，或者修改数据库
        // 为了跑通，我们假设 Order 表里有 refund_reason 字段来暂时存地址信息展示用，或者忽略
        // 建议：此处仅做演示，不做地址入库，重点在逻辑

        this.save(order);

        // 5. 保存订单详情
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItems);

        // 6. 清空购物车中已结算的商品
        cartService.removeByIds(createDTO.getCartIds());

        return orderSn;
    }

    @Override
    public void payOrder(String orderSn) {
        Order order = getOrderBySn(orderSn);

        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不可支付");
        }

        order.setStatus((byte) 1); // 1-待发货
        order.setPayTime(LocalDateTime.now());
        // 模拟支付全额
        order.setPayAmount(order.getTotalAmount());

        this.updateById(order);
    }

    @Override
    public void cancelOrder(String orderSn) {
        Order order = getOrderBySn(orderSn);

        // 只有待支付状态才能取消
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前状态无法取消");
        }

        // 1. 改状态
        order.setStatus((byte) -1); // -1 已取消
        order.setCancelReason("用户主动取消");
        this.updateById(order);

        // 2. (加分项) 应该把库存还回去
        // 这里作为思考题，你需要查询 orderItems 然后把 quantity 加回 product stock
        restoreStock(order.getId());
    }

    // 辅助方法：库存回滚
    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            productService.update(
                    new LambdaUpdateWrapper<Product>()
                            .setSql("stock = stock + " + item.getQuantity())
                            .eq(Product::getId, item.getProductId())
            );
        }
    }

    @Override
    public Page<OrderVO> myOrderList(Integer pageNum, Integer pageSize, Integer status) {
        Long userId = SecurityUtils.getUserId();
        Page<Order> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        this.page(page, wrapper);

        // 转换 VO
        Page<OrderVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage);

        List<OrderVO> voList = page.getRecords().stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);

            // 查子项
            List<OrderItem> items = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            vo.setOrderItems(items);
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    private Order getOrderBySn(String orderSn) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderSn, orderSn);
        Order order = this.getOne(wrapper);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        // 只能操作自己的订单
        if (!order.getUserId().equals(SecurityUtils.getUserId())) {
            throw new RuntimeException("无权操作此订单");
        }
        return order;
    }
}