package com.sc.mall.common.context;

public class ShopContextHolder {
    private static final ThreadLocal<Long> SHOP_ID_HOLDER = new ThreadLocal<>();

    public static void setShopId(Long shopId) { SHOP_ID_HOLDER.set(shopId); }
    public static Long getShopId() { return SHOP_ID_HOLDER.get(); }
    public static void clear() { SHOP_ID_HOLDER.remove(); }
}
