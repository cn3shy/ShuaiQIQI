package com.shuaiqi.gateway.filter;

import com.shuaiqi.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/content/list",
            "/api/content/categories",
            "/api/content/recommend",
            "/api/content/hot"
    );

    private static final List<String> ADMIN_PATHS = List.of(
            "/api/admin/",
            "/api/user/list",
            "/api/user/count",
            "/api/content/count",
            "/api/content/likes/count",
            "/api/comment/count"
    );

    private static final Pattern CONTENT_DETAIL_PATTERN = Pattern.compile("^/api/content/\\d+$");
    private static final Pattern COMMENT_CONTENT_PATTERN = Pattern.compile("^/api/comment/content/\\d+$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);
        if (token == null || !JwtUtils.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 从 JWT Token 中提取用户信息，而不是信任客户端传入的 Header
        String userId = JwtUtils.getUserId(token);
        String role = JwtUtils.getRole(token);

        if (isAdminPath(path) && !"admin".equals(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 仅设置由 JWT 解析得到的用户信息，防止 Header 注入攻击
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role != null ? role : "user")
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhiteListed(String path) {
        if (CONTENT_DETAIL_PATTERN.matcher(path).matches() || COMMENT_CONTENT_PATTERN.matcher(path).matches()) {
            return true;
        }
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream().anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
