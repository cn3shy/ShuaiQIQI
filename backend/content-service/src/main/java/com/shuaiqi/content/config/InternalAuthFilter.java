package com.shuaiqi.content.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Order(1)
@Component
public class InternalAuthFilter implements Filter {

    @Value("${internal.service-key:}")
    private String serviceKey;

    private static final String INTERNAL_PATH = "/api/content/{contentId}/comment-count";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        if (path.matches("/api/content/\\d+/comment-count") && !serviceKey.isEmpty()) {
            String key = httpRequest.getHeader("X-Internal-Key");
            if (!serviceKey.equals(key)) {
                log.warn("服务间调用认证失败: path={}", path);
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
