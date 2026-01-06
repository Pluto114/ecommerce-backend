package com.sc.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.mall.entity.Banner;
import com.sc.mall.mapper.BannerMapper;
import com.sc.mall.service.IBannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService {
}
