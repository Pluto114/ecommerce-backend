package com.sc.mall.service.impl;

import com.sc.mall.entity.Product;
import com.sc.mall.mapper.ProductMapper;
import com.sc.mall.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品信息表 服务实现类
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

}
