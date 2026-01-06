package com.sc.mall.service.impl;

import com.sc.mall.entity.Shop;
import com.sc.mall.mapper.ShopMapper;
import com.sc.mall.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
}