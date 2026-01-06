package com.sc.mall.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();
        if (principal == null) return null;

        // 我们在 JwtFilter 里把 userId 放到了 principal
        if (principal instanceof Long) return (Long) principal;
        if (principal instanceof String) return Long.valueOf((String) principal);

        return null;
    }

    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;

        // 我们在 JwtFilter 里把 username 放到了 details
        Object details = authentication.getDetails();
        if (details != null) return details.toString();

        // 兜底：Spring Security 默认 name
        return authentication.getName();
    }
}
