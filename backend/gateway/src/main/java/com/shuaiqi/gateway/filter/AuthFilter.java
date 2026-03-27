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

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    // 不需要认证的路径
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/content/list",
            "/api/content/categories",
            "/api/content/recommend",
            "/api/content/hot",
            "/api/user/"
    );

    // 需要管理员权限的路径
    private static final List<String> ADMIN_PATHS = List.of(
            "/api/user/list",
            "/api/user/delete"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径直接放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 获取Token
        String token = extractToken(request);
        if (token == null || !JwtUtils.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 获取用户ID和角色
        String userId = JwtUtils.getUserId(token);
        String role = JwtUtils.getRole(token);

        // 检查管理员权限
        if (isAdminPath(path) && !"admin".equals(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 将用户信息添加到请求头
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

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        // 获取内容详情允许匿名访问
        if (path.matches("/api/content/\\d+") || path.matches("/api/comment/content/\\d+")) {
            return true;
        }
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 检查路径是否需要管理员权限
     */
    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
