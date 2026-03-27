package com.shuaiqi.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtils {

    private static final String SECRET_KEY = "shuaiqi-qi-secret-key-2024-01-01";
    private static final long ACCESS_TOKEN_EXPIRE = 3600 * 1000L * 24; // 24小时
    private static final long REFRESH_TOKEN_EXPIRE = 3600 * 1000L * 24 * 7; // 7天

    // 私钥
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成访问令牌
     */
    public static String generateAccessToken(String userId, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    public static String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户ID
     */
    public static String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取用户角色
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("role");
    }
}
