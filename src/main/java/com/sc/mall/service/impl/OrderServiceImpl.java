package com.sc.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.mall.common.context.ShopContextHolder; // ✅ 第1步你新增的上下文（按你的实际包名改）
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Resource
    private ICartService cartService;
    @Resource
    private IProductService productService;
    @Resource
    private IOrderItemService orderItemService;
    @Resource
    private IShopService shopService;
    @Resource
    private IOrderAddressService orderAddressService;

    // ========= 状态常量（与 Order.status 注释一致） =========
    private static final byte ST_WAIT_PAY = 0;        // 待支付
    private static final byte ST_WAIT_SHIP = 1;       // 已支付/待发货
    private static final byte ST_WAIT_RECEIVE = 2;    // 已发货/待收货
    private static final byte ST_WAIT_COMMENT = 3;    // 已收货/待评价
    private static final byte ST_FINISH = 4;          // 已评价/已完成

    private static final byte ST_CANCEL = -1;         // 已取消
    private static final byte ST_REFUND_APPLY = -2;   // 申请退单
    private static final byte ST_REFUND_REJECT = -3;  // 拒绝退单
    private static final byte ST_REFUND_DONE = -4;    // 退单成功（退款完成）

    // =========================================================
    // 1) 创建订单（核心事务）
    //    ✅ 多店铺适配：必须写入 order.shopId + orderItem.shopId
    //    ✅ 最小改动策略：一次结算只允许同一店铺商品
    // =========================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderCreateDTO createDTO) {
        Long userId = SecurityUtils.getUserId();

        // 1. 查询勾选的购物车商品
        List<Cart> cartList = cartService.listByIds(createDTO.getCartIds());
        if (cartList == null || cartList.isEmpty()) {
            throw new RuntimeException("请选择要结算的商品");
        }

        // 2. 校验购物车归属
        for (Cart cart : cartList) {
            if (!Objects.equals(cart.getUserId(), userId)) {
                throw new RuntimeException("非法操作：购物车信息有误");
            }
        }

        // 3. 准备商品Map
        List<Long> productIds = cartList.stream().map(Cart::getProductId).collect(Collectors.toList());
        List<Product> products = productService.listByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        // ✅ 3.1 校验：一次结算必须同一店铺
        Set<Long> shopIdSet = new HashSet<>();
        for (Cart cart : cartList) {
            Product p = productMap.get(cart.getProductId());
            if (p == null) {
                throw new RuntimeException("部分商品已下架或不存在");
            }
            if (p.getShopId() == null) {
                throw new RuntimeException("商品 [" + p.getName() + "] 未绑定店铺，无法下单");
            }
            shopIdSet.add(p.getShopId());
        }
        if (shopIdSet.size() != 1) {
            throw new RuntimeException("一次只能结算同一店铺的商品，请分开结算");
        }
        Long shopId = shopIdSet.iterator().next();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 4. 生成订单号
        String orderSn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        // 5. 循环处理每个商品：校验库存、扣库存、算钱、生成快照
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

            // A. 扣减库存（数据库层面防超卖）
            boolean updateResult = productService.update(
                    new LambdaUpdateWrapper<Product>()
                            .setSql("stock = stock - " + cart.getQuantity())
                            .eq(Product::getId, product.getId())
                            .ge(Product::getStock, cart.getQuantity())
            );
            if (!updateResult) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足(并发拦截)");
            }

            // B. 累加总价
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // C. 构建订单详情快照
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImageUrl());
            item.setProductPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());

            // ✅ 多店铺：订单项也写 shop_id（你表已加）
            //    若你的 OrderItem 实体字段名不是 shopId，请你告诉我，我再按你的实体改
            item.setShopId(shopId);

            orderItems.add(item);
        }

        // 6. 保存主订单
        Order order = new Order();
        order.setOrderSn(orderSn);
        order.setUserId(userId);

        // ✅ 多店铺：订单主表写 shop_id（你表已加 NOT NULL）
        order.setShopId(shopId);

        order.setTotalAmount(totalAmount);
        order.setPayAmount(null);
        order.setPointsUsed(0);
        order.setStatus(ST_WAIT_PAY);

        this.save(order);

        // 6.1 保存收货信息（新表 tb_order_address）
        OrderAddress oa = new OrderAddress();
        oa.setOrderId(order.getId());
        oa.setReceiverName(createDTO.getReceiverName());
        oa.setReceiverPhone(createDTO.getReceiverPhone());
        oa.setAddress(createDTO.getAddress());
        orderAddressService.save(oa);

        // 7. 保存订单详情
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItems);

        // 8. 清空购物车中已结算商品
        cartService.removeByIds(createDTO.getCartIds());

        return orderSn;
    }

    // =========================================================
    // 2) 模拟支付：0 -> 1
    // =========================================================
    @Override
    public void payOrder(String orderSn) {
        Order order = getMyOrderBySn(orderSn);

        if (order.getStatus() != ST_WAIT_PAY) {
            throw new RuntimeException("订单状态不可支付");
        }

        boolean ok = this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderSn, orderSn)
                .eq(Order::getUserId, SecurityUtils.getUserId())
                .eq(Order::getStatus, ST_WAIT_PAY)
                .set(Order::getStatus, ST_WAIT_SHIP)
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getPayAmount, order.getTotalAmount())
        );

        if (!ok) {
            throw new RuntimeException("支付失败：订单状态已变化，请刷新重试");
        }
    }

    // =========================================================
    // 3) 取消订单：0 -> -1，并回滚库存
    // =========================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderSn) {
        Order order = getMyOrderBySn(orderSn);

        if (order.getStatus() != ST_WAIT_PAY) {
            throw new RuntimeException("当前状态无法取消");
        }

        boolean ok = this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderSn, orderSn)
                .eq(Order::getUserId, SecurityUtils.getUserId())
                .eq(Order::getStatus, ST_WAIT_PAY)
                .set(Order::getStatus, ST_CANCEL)
                .set(Order::getCancelReason, "用户主动取消")
        );

        if (!ok) {
            throw new RuntimeException("取消失败：订单状态已变化，请刷新重试");
        }

        // 回滚库存（加分项）
        restoreStock(order.getId());
    }

    // =========================================================
    // 4) 我的订单列表（分页 + 批量回填 items + address + receiverInfo/statusText）
    // =========================================================
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

        return buildOrderVOPageWithAddress(page, null, null);
    }

    // =========================================================
    // 5) 用户确认收货：2 -> 3
    // =========================================================
    @Override
    public void receiveOrder(String orderSn) {
        Order order = getMyOrderBySn(orderSn);

        if (order.getStatus() != ST_WAIT_RECEIVE) {
            throw new RuntimeException("当前状态无法确认收货");
        }

        boolean ok = this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderSn, orderSn)
                .eq(Order::getUserId, SecurityUtils.getUserId())
                .eq(Order::getStatus, ST_WAIT_RECEIVE)
                .set(Order::getStatus, ST_WAIT_COMMENT)
                .set(Order::getReceiveTime, LocalDateTime.now())
        );

        if (!ok) {
            throw new RuntimeException("确认收货失败：订单状态已变化，请刷新重试");
        }
    }

    // =========================================================
    // 6) 用户评价完成：3 -> 4
    // =========================================================
    @Override
    public void finishComment(String orderSn) {
        Order order = getMyOrderBySn(orderSn);

        if (order.getStatus() != ST_WAIT_COMMENT) {
            throw new RuntimeException("当前状态无法评价完成");
        }

        boolean ok = this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderSn, orderSn)
                .eq(Order::getUserId, SecurityUtils.getUserId())
                .eq(Order::getStatus, ST_WAIT_COMMENT)
                .set(Order::getStatus, ST_FINISH)
                .set(Order::getCommentTime, LocalDateTime.now())
        );

        if (!ok) {
            throw new RuntimeException("评价完成失败：订单状态已变化，请刷新重试");
        }
    }

    // =========================================================
    // 7) 用户申请退单：1/2 -> -2
    // =========================================================
    @Override
    public void applyRefund(String orderSn, String refundReason) {
        Order order = getMyOrderBySn(orderSn);
        byte cur = order.getStatus();

        if (!(cur == ST_WAIT_SHIP || cur == ST_WAIT_RECEIVE)) {
            throw new RuntimeException("当前状态无法申请退单");
        }

        boolean ok = this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderSn, orderSn)
                .eq(Order::getUserId, SecurityUtils.getUserId())
                .eq(Order::getStatus, cur)
                .set(Order::getStatus, ST_REFUND_APPLY)
                .set(Order::getRefundReason, refundReason)
        );

        if (!ok) {
            throw new RuntimeException("退单申请失败：订单状态已变化，请刷新重试");
        }
    }

    // =========================================================
    // 8) 商家发货：1 -> 2
    //    ✅ 多店铺：按当前 ShopContext 的 shopId 鉴权：order.shopId 必须等于当前 shopId
    // =========================================================
    @Override
    public void shipOrder(String orderSn) {
        Order order = getAnyOrderBySn(orderSn);

        // ✅ 新：店铺维度鉴权（拦截器已经保证 shopId 属于当前商家）
        assertOrderInCurrentShop(order);

        if (order.getStatus() != ST_WAIT_SHIP) {
            throw new RuntimeException("当前状态无法发货");
        }

        boolean ok = this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderSn, orderSn)
                .eq(Order::getShopId, ShopContextHolder.getShopId())
                .eq(Order::getStatus, ST_WAIT_SHIP)
                .set(Order::getStatus, ST_WAIT_RECEIVE)
                .set(Order::getShippingTime, LocalDateTime.now())
        );

        if (!ok) {
            throw new RuntimeException("发货失败：订单状态已变化，请刷新重试");
        }
    }

    // =========================================================
    // 9) 商家审核退单：-2 -> -3/-4
    //    ✅ 多店铺：按当前 ShopContext 的 shopId 鉴权
    // =========================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(String orderSn, boolean approve, String adminReason) {
        Order order = getAnyOrderBySn(orderSn);

        // ✅ 新：店铺维度鉴权
        assertOrderInCurrentShop(order);

        if (order.getStatus() != ST_REFUND_APPLY) {
            throw new RuntimeException("当前状态无法审核退单");
        }

        byte target = approve ? ST_REFUND_DONE : ST_REFUND_REJECT;

        boolean ok = this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderSn, orderSn)
                .eq(Order::getShopId, ShopContextHolder.getShopId())
                .eq(Order::getStatus, ST_REFUND_APPLY)
                .set(Order::getStatus, target)
                .set(Order::getRefundAdminReason, adminReason)
        );

        if (!ok) {
            throw new RuntimeException("审核失败：订单状态已变化，请刷新重试");
        }

        // 同意退单：库存回滚（加分项）
        if (approve) {
            restoreStock(order.getId());
        }
    }

    // =========================================================
    // 10) 商家订单列表：按当前 shopId 直接分页查询 tb_order
    //     ✅ 多店铺：不再通过 “商家->店铺->商品->订单项->订单” 反查，直接用 order.shop_id
    // =========================================================
    @Override
    public Page<OrderVO> merchantOrderList(Integer pageNum, Integer pageSize, Integer status, String orderSn) {
        Long shopId = ShopContextHolder.getShopId();
        if (shopId == null) {
            throw new RuntimeException("缺少店铺上下文，请先选择店铺");
        }

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getShopId, shopId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (orderSn != null && !orderSn.isBlank()) {
            wrapper.like(Order::getOrderSn, orderSn);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        this.page(page, wrapper);

        // ✅ 商家端：订单项只回填当前店铺的（用 order_item.shop_id 过滤，避免泄漏）
        return buildOrderVOPageWithAddress(page, shopId, null);
    }

    // =========================================================
    // 批量回填：订单项 + 收货地址 + receiverInfo/statusText
    // merchantShopId != null 时：仅返回属于该店铺的订单项（用 order_item.shop_id 过滤）
    // =========================================================
    private Page<OrderVO> buildOrderVOPageWithAddress(Page<Order> page, Long merchantShopId, Set<Long> myProductIdSetLegacy) {

        Page<OrderVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage);
        if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        List<Order> orders = page.getRecords();
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .filter(Objects::nonNull)
                .toList();

        // 2) 批量查询订单项
        List<OrderItem> allItems = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds)
        );

        // 2.1) 商家端：仅保留当前店铺的订单项（优先用 shop_id）
        if (merchantShopId != null && allItems != null) {
            allItems = allItems.stream()
                    .filter(oi -> oi.getShopId() != null && Objects.equals(oi.getShopId(), merchantShopId))
                    .toList();
        } else if (myProductIdSetLegacy != null && !myProductIdSetLegacy.isEmpty() && allItems != null) {
            // 兼容你旧逻辑（如果某些历史订单项没补 shop_id，可走这个兜底）
            allItems = allItems.stream()
                    .filter(oi -> oi.getProductId() != null && myProductIdSetLegacy.contains(oi.getProductId()))
                    .toList();
        }

        Map<Long, List<OrderItem>> orderItemMap = (allItems == null ? Collections.<OrderItem>emptyList() : allItems)
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 3) 批量查询收货地址（tb_order_address）
        List<OrderAddress> addressList = orderAddressService.list(
                new LambdaQueryWrapper<OrderAddress>().in(OrderAddress::getOrderId, orderIds)
        );
        Map<Long, OrderAddress> addressMap = (addressList == null ? Collections.<OrderAddress>emptyList() : addressList)
                .stream()
                .collect(Collectors.toMap(OrderAddress::getOrderId, a -> a, (a, b) -> a));

        // 4) 组装 VO
        List<OrderVO> voList = orders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);

            // ✅ Byte -> Integer
            vo.setStatus(order.getStatus() == null ? null : order.getStatus().intValue());

            // 回填订单项
            List<OrderItem> items = orderItemMap.getOrDefault(order.getId(), Collections.emptyList());
            vo.setOrderItems(items);

            // 回填地址
            OrderAddress oa = addressMap.get(order.getId());
            if (oa != null) {
                vo.setReceiverName(oa.getReceiverName());
                vo.setReceiverPhone(oa.getReceiverPhone());
                vo.setAddress(oa.getAddress());

                String info = String.format("%s %s %s",
                        oa.getReceiverName() == null ? "" : oa.getReceiverName(),
                        oa.getReceiverPhone() == null ? "" : oa.getReceiverPhone(),
                        oa.getAddress() == null ? "" : oa.getAddress()
                ).trim();
                vo.setReceiverInfo(info);
            }

            vo.setStatusText(getStatusTextSafe(vo.getStatus()));
            return vo;
        }).toList();

        voPage.setRecords(voList);
        return voPage;
    }

    private String getStatusTextSafe(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "待评价";
            case 4 -> "已完成";
            case -1 -> "已取消";
            case -2 -> "退单中";
            case -3 -> "退单拒绝";
            case -4 -> "退单成功";
            default -> "未知";
        };
    }

    private String buildReceiverInfo(String name, String phone, String address) {
        String n = name == null ? "" : name;
        String p = phone == null ? "" : phone;
        String a = address == null ? "" : address;
        String s = (n + " " + p + " " + a).trim();
        return s.isBlank() ? null : s;
    }

    private String mapStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "待评价";
            case 4 -> "已完成";
            case -1 -> "已取消";
            case -2 -> "申请退单";
            case -3 -> "拒绝退单";
            case -4 -> "退单成功";
            default -> "未知状态";
        };
    }

    // =========================================================
    // 辅助：库存回滚
    // =========================================================
    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            productService.update(new LambdaUpdateWrapper<Product>()
                    .setSql("stock = stock + " + item.getQuantity())
                    .eq(Product::getId, item.getProductId())
            );
        }
    }

    // =========================================================
    // ✅ 新增：店铺维度鉴权
    // 规则：当前 ShopContext 的 shopId 必须等于 order.shopId
    // =========================================================
    private void assertOrderInCurrentShop(Order order) {
        Long curShopId = ShopContextHolder.getShopId();
        if (curShopId == null) {
            throw new RuntimeException("缺少店铺上下文，请先选择店铺");
        }
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getShopId() == null) {
            // 理论上不应出现（你已 NOT NULL），做个兜底
            throw new RuntimeException("订单未绑定店铺，无法操作");
        }
        if (!Objects.equals(order.getShopId(), curShopId)) {
            throw new RuntimeException("无权操作该订单：不属于当前店铺");
        }
    }

    // =========================================================
    // 辅助：用户侧取单（校验归属）
    // =========================================================
    private Order getMyOrderBySn(String orderSn) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderSn, orderSn));
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!Objects.equals(order.getUserId(), SecurityUtils.getUserId())) {
            throw new RuntimeException("无权操作此订单");
        }
        return order;
    }

    // =========================================================
    // 辅助：取单（不校验归属）
    // =========================================================
    private Order getAnyOrderBySn(String orderSn) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderSn, orderSn));
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    @Override
    public OrderVO merchantOrderDetail(String orderSn) {
        Order order = getAnyOrderBySn(orderSn);

        // ✅ 按当前店铺鉴权
        assertOrderInCurrentShop(order);

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatus(order.getStatus() == null ? null : order.getStatus().intValue());

        Long shopId = ShopContextHolder.getShopId();

        // ✅ 订单项：只返回当前店铺的（用 order_item.shop_id）
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId())
                        .eq(OrderItem::getShopId, shopId)
        );
        vo.setOrderItems(items);

        // 地址
        OrderAddress addr = orderAddressService.getOne(
                new LambdaQueryWrapper<OrderAddress>().eq(OrderAddress::getOrderId, order.getId())
        );
        if (addr != null) {
            vo.setReceiverName(addr.getReceiverName());
            vo.setReceiverPhone(addr.getReceiverPhone());
            vo.setAddress(addr.getAddress());
            vo.setReceiverInfo(buildReceiverInfo(addr.getReceiverName(), addr.getReceiverPhone(), addr.getAddress()));
        }

        vo.setStatusText(mapStatusText(vo.getStatus()));
        return vo;
    }

    @Override
    public OrderVO myOrderDetail(String orderSn) {
        Order order = getMyOrderBySn(orderSn);

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatus(order.getStatus() == null ? null : order.getStatus().intValue());

        // items（用户端返回全部）
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
        );
        vo.setOrderItems(items);

        // address
        OrderAddress addr = orderAddressService.getOne(
                new LambdaQueryWrapper<OrderAddress>().eq(OrderAddress::getOrderId, order.getId())
        );
        if (addr != null) {
            vo.setReceiverName(addr.getReceiverName());
            vo.setReceiverPhone(addr.getReceiverPhone());
            vo.setAddress(addr.getAddress());
            vo.setReceiverInfo(buildReceiverInfo(addr.getReceiverName(), addr.getReceiverPhone(), addr.getAddress()));
        }

        vo.setStatusText(mapStatusText(vo.getStatus()));
        return vo;
    }
}
