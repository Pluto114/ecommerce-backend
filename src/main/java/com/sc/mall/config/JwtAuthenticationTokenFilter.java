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

/**
 * JWT 登录授权过滤器
 * 作用：拦截请求，解析 Token，设置登录状态
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头中获取 Token
        String token = request.getHeader("Authorization");

        // 2. 判断 Token 是否存在且格式正确 (通常是 "Bearer " 开头)
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            // 如果没有 Token，直接放行 (交给 Spring Security 后面的过滤器去拦截未登录请求)
            filterChain.doFilter(request, response);
            return;
        }

        // 去掉 "Bearer " 前缀
        token = token.substring(7);

        try {
            // 3. 解析 Token
            Claims claims = jwtUtil.parseToken(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            Integer role = claims.get("role", Integer.class);

            // 4. 构建权限信息 (角色)
            // Spring Security 需要权限列表，我们把 role 转换一下
            // 约定：角色名格式通常为 "ROLE_ADMIN", "ROLE_USER" 等
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (role != null) {
                // 这里简单映射：1-ADMIN, 2-MANAGER, 3-USER
                String roleName = "";
                switch (role) {
                    case 1: roleName = "ROLE_ADMIN"; break;
                    case 2: roleName = "ROLE_MANAGER"; break;
                    case 3: roleName = "ROLE_USER"; break;
                    default: roleName = "ROLE_UNKNOWN";
                }
                authorities.add(new SimpleGrantedAuthority(roleName));
            }

            // 5. 组装认证对象 (UsernamePasswordAuthenticationToken)
            // 参数：用户信息(principal), 密码(credentials, 已登录不需要), 权限(authorities)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 将 userId 等额外信息存入 details，方便后续业务获取
            authentication.setDetails(userId);

            // 6. 存入 SecurityContext (关键一步！告诉 Spring Security "已登录")
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.error("Token认证失败: {}", e.getMessage());
            // 如果 Token 过期或非法，这里不抛异常，直接放行，让 Spring Security 报 403
        }

        // 7. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}