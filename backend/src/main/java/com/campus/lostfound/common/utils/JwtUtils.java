package com.campus.lostfound.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {
    @Value("${app.jwt.secret}")
    private String secret;

    // private static final String SECRET = "CampusLostAndFoundSystemSuperSecretKey2026!";

    // 实际项目中，建议把这个密钥配置在 application.yml 中 (@Value)
    // 注意：HS256 算法要求密钥长度至少为 256 bits (即 32 个英文字符)

    // private static final String SECRET = "CampusLostAndFoundSystemSuperSecretKey2026!";
    
    // 生成标准的 SecretKey 对象
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    // Access Token 过期时间：2小时
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 2;
    
    // Refresh Token 过期时间：7天
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String studentNo) {
        return Jwts.builder()
                .subject(String.valueOf(userId))    // 0.12.x: setSubject -> subject
                .claim("studentNo", studentNo)
                .issuedAt(new Date())               // 0.12.x: setIssuedAt -> issuedAt
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION)) // setExpiration -> expiration
                .signWith(SECRET_KEY)               // 传入 SecretKey
                .compact();
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析 Token 获取 Claims (0.12.x 最新语法)
     */
    public Claims parseToken(String token) {
        return Jwts.parser()                        // 0.12.x: 恢复使用 parser()
                .verifyWith(SECRET_KEY)             // 0.12.x: setSigningKey -> verifyWith
                .build()
                .parseSignedClaims(token)           // 0.12.x: parseClaimsJws -> parseSignedClaims
                .getPayload();                      // 0.12.x: getBody -> getPayload
    }

    /**
     * 从 Token 中获取 userId
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }
}
