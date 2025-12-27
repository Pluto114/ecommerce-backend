package com.sc.mall.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类，用于获取当前登录用户的信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID
     * @return userId
     */
    public static Long getUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // 在 JwtFilter 里我们把 userId 存到了 Details 中
            // 如果 filter 里存的是 principal，这里就改用 getPrincipal
            Object details = authentication.getDetails();
            if (details != null) {
                return Long.valueOf(details.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("获取当前登录用户失败");
        }
        return null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}