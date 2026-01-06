package com.sc.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.mall.entity.OrderAddress;
import com.sc.mall.mapper.OrderAddressMapper;
import com.sc.mall.service.IOrderAddressService;
import org.springframework.stereotype.Service;

@Service
public class OrderAddressServiceImpl extends ServiceImpl<OrderAddressMapper, OrderAddress>
        implements IOrderAddressService {
}
