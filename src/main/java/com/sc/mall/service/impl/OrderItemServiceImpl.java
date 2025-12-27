package com.sc.mall.service.impl;

import com.sc.mall.entity.OrderItem;
import com.sc.mall.mapper.OrderItemMapper;
import com.sc.mall.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单详情表 (商品快照) 服务实现类
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

}
