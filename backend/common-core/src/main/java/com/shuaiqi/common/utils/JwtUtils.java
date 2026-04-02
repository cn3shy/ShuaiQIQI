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

@Slf4j
public class JwtUtils {

    private static final String SECRET_KEY = System.getenv().getOrDefault("JWT_SECRET_KEY", "shuaiqi-qi-secret-key-2024-01-01-must-be-at-least-256-bits-long-for-hs256");
    private static final long ACCESS_TOKEN_EXPIRE = 3600 * 1000L * 24;
    private static final long REFRESH_TOKEN_EXPIRE = 3600 * 1000L * 24 * 7;

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    public static String generateAccessToken(String userId, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public static String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("role");
    }
}
