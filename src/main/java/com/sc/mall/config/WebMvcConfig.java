package com.sc.mall.config;

import com.sc.mall.common.interceptor.MerchantShopInterceptor;
import com.sc.mall.service.IShopService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private IShopService shopService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MerchantShopInterceptor(shopService))
                .addPathPatterns("/merchant/**"); // 只拦商家端
    }
}
