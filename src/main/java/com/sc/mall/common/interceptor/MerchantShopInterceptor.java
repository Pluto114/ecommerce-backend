package com.sc.mall.common.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.mall.common.context.ShopContextHolder;
import com.sc.mall.common.utils.SecurityUtils;
import com.sc.mall.entity.Shop;
import com.sc.mall.service.IShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class MerchantShopInterceptor implements HandlerInterceptor {

    private final IShopService shopService;

    public MerchantShopInterceptor(IShopService shopService) {
        this.shopService = shopService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String shopIdStr = request.getHeader("X-Shop-Id");
        if (!StringUtils.hasText(shopIdStr)) {
            response.setStatus(400);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"msg\":\"缺少X-Shop-Id，请先选择店铺\"}");
            return false;
        }

        Long shopId;
        try {
            shopId = Long.valueOf(shopIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"msg\":\"X-Shop-Id格式错误\"}");
            return false;
        }

        Long adminId = SecurityUtils.getUserId();

        boolean owned = shopService.exists(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getId, shopId)
                .eq(Shop::getAdminId, adminId)
        );
        if (!owned) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"你无权操作该店铺\"}");
            return false;
        }

        ShopContextHolder.setShopId(shopId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ShopContextHolder.clear();
    }
}
