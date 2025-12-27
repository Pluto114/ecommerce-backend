package com.sc.mall.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 定义一个白名单，这些路径将不需要登录即可访问
     */
    private final String[] whiteListUrls = {
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/auth/login",
            "/auth/register",
            "/test/**" // 临时放行测试接口，或者你可以把它删掉测试 403
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF (Token模式不需要)
                .csrf(csrf -> csrf.disable())

                // 2. 开启跨域支持 (前端 Vue 可能会跨域)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. 禁用 Session (关键！使用 JWT 必须设为无状态)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 配置 URL 授权
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(whiteListUrls).permitAll() // 白名单放行
                        .anyRequest().authenticated() // 其他接口需认证
                )

                // 5. 添加 JWT 过滤器 (在 用户名密码过滤器 之前执行)
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置跨域 (CORS)
     * 允许前端 8080/5173 等端口访问后端
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 允许访问的源，生产环境需指定具体域名，开发环境可配为 "*" (但 setAllowCredentials(true) 时不能用 "*")
        // 这里使用 allowedOriginPatterns 配合 *
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    // 辅助方法：供 Security 使用
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}