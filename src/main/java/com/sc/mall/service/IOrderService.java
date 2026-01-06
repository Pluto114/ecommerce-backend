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
     * 注意：当前实现为【同一店铺商品】一次结算生成一个订单；
     * 若购物车混入多个店铺商品，会提示分开结算（后续可升级按店铺拆单）。
     *
     * @param createDTO 下单参数
     * @return 订单编号
     */
    String createOrder(OrderCreateDTO createDTO);

    void payOrder(String orderSn);

    Page<OrderVO> myOrderList(Integer pageNum, Integer pageSize, Integer status);

    void cancelOrder(String orderSn);

    void receiveOrder(String orderSn);

    void finishComment(String orderSn);

    void applyRefund(String orderSn, String refundReason);

    void shipOrder(String orderSn);

    void auditRefund(String orderSn, boolean approve, String adminReason);

    /**
     * 商家订单列表：按【当前选择的店铺 shopId】过滤
     * shopId 由第1步拦截器注入 ShopContext
     */
    Page<OrderVO> merchantOrderList(Integer pageNum, Integer pageSize, Integer status, String orderSn);

    OrderVO myOrderDetail(String orderSn);

    /**
     * 商家订单详情：按【当前选择的店铺 shopId】过滤
     */
    OrderVO merchantOrderDetail(String orderSn);
}
