package com.sc.mall.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    /**
     * ✅ 建议部署时配置环境变量 JWT_SECRET
     * 长度必须足够（HS256 至少 32 字节），下面给了默认值兜底
     */
    private static final String SECRET =
            System.getenv().getOrDefault("JWT_SECRET", "sc-mall-jwt-secret-sc-mall-jwt-secret");

    // 24小时有效期
    private static final long EXPIRE_MS = 24L * 60 * 60 * 1000;

    private Key key() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ✅ 兼容你原代码：UserServiceImpl 调用 createToken(id, username, role)
     */
    public String createToken(Long userId, String username, int role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRE_MS);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * ✅ 兼容你原代码：JwtAuthenticationTokenFilter 调用 parseToken(token)
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ 可选：如果你其他地方用了 parse(token)，也一并兼容
     */
    public Claims parse(String token) {
        return parseToken(token);
    }
}
