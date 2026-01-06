package com.sc.mall.config;

import com.sc.mall.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7);

        try {
            Claims claims = jwtUtil.parseToken(token);

            String userIdStr = claims.getSubject();
            String username = claims.get("username", String.class);
            Integer role = claims.get("role", Integer.class);

            Long userId = Long.valueOf(userIdStr);

            // 角色 -> authorities
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (role != null) {
                String roleName;
                switch (role) {
                    case 1: roleName = "ROLE_ADMIN"; break;
                    case 2: roleName = "ROLE_MANAGER"; break;
                    case 3: roleName = "ROLE_USER"; break;
                    default: roleName = "ROLE_UNKNOWN";
                }
                authorities.add(new SimpleGrantedAuthority(roleName));
            }

            // ✅ 关键：principal 放 userId（业务最常用）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // ✅ details 放 username（可选，但 SecurityUtils.getUsername 会用到）
            authentication.setDetails(username);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.error("Token认证失败: {}", e.getMessage());
            // token 非法/过期：放行，交给后续 security 拦截
        }

        filterChain.doFilter(request, response);
    }
}
