package com.shuaiqi.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtils {

    private static final String SECRET_KEY;
    private static final SecretKey KEY;

    static {
        String envKey = System.getenv("JWT_SECRET_KEY");
        if (envKey == null || envKey.isBlank()) {
            throw new IllegalStateException("JWT_SECRET_KEY 环境变量未配置，请在启动前设置该环境变量");
        }
        if (envKey.length() < 32) {
            throw new IllegalStateException("JWT_SECRET_KEY 长度不能少于32字符");
        }
        SECRET_KEY = envKey;
        KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        log.info("JWT密钥已初始化，长度: {}", SECRET_KEY.length());
    }

    private static final long ACCESS_TOKEN_EXPIRE = 7 * 1000L * 24;
    private static final long REFRESH_TOKEN_EXPIRE = 7 * 1000L * 24 * 7;

    public static String generateAccessToken(String userId, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(KEY, Jwts.SIG.HS256)
                .compact();
    }

    public static String generateRefreshToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(KEY, Jwts.SIG.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
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
