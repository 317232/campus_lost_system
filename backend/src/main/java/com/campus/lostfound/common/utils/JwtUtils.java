package com.campus.lostfound.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {
    @Value("${app.jwt.secret}")
    private String secret;

    private SecretKey getSecretKey() {
        // secret 在 application.yml 中是 base64 编码，需先解码
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Access Token 过期时间：2小时
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 2;
    
    // Refresh Token 过期时间：7天
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String studentNo, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("studentNo", studentNo)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSecretKey())
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
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 Token 获取 Claims (0.12.x 最新语法)
     */
    public Claims parseToken(String token) {
        return Jwts.parser()                        // 0.12.x: 恢复使用 parser()
                .verifyWith(getSecretKey())             // 0.12.x: setSigningKey -> verifyWith
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