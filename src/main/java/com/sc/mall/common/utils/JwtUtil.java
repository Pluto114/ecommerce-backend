package com.sc.mall.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    // 密钥 (实际开发中应放在 application.yml，且必须足够长，至少32个字符)
    private static final String SECRET = "ScMallProjectSecurityKeyForJwtTokenGeneration2025";
    // 过期时间：24小时 (毫秒)
    private static final long EXPIRATION = 24 * 60 * 60 * 1000L;

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @return Token 字符串
     */
    public String createToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(userId.toString()) // 主题存 UserID
                .claim("username", username)   // 存用户名
                .claim("role", role)           // 存角色
                .setIssuedAt(now)              // 签发时间
                .setExpiration(expiryDate)     // 过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    /**
     * 解析 Token 获取 Claims (包含所有负载信息)
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT解析失败: {}", e.getMessage());
            throw new RuntimeException("Token无效或已过期");
        }
    }

    /**
     * 从 Token 中获取 UserID
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }
}