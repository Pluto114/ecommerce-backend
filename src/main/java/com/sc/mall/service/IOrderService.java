package com.sc.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sc.mall.entity.Order;
import com.sc.mall.entity.dto.OrderCreateDTO;
import com.sc.mall.entity.vo.OrderVO;

/**
 * <p>
 * 订单表 服务类
 * </p>
 */
public interface IOrderService extends IService<Order> {

    /**
     * 创建订单 (核心事务)
     * @param createDTO 下单参数
     * @return 订单编号
     */
    String createOrder(OrderCreateDTO createDTO);

    /**
     * 模拟支付
     * @param orderSn 订单号
     */
    void payOrder(String orderSn);

    /**
     * 分页查询我的订单
     * @param pageNum 页码
     * @param pageSize 条数
     * @param status 状态筛选 (可选)
     * @return 订单VO列表
     */
    Page<OrderVO> myOrderList(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 取消订单
     */
    void cancelOrder(String orderSn);
}