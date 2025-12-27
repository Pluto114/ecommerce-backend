package com.sc.mall.service.impl;

import com.sc.mall.entity.Cart;
import com.sc.mall.mapper.CartMapper;
import com.sc.mall.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

}
