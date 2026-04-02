package com.shuaiqi.comment.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeignInternalAuthInterceptor implements RequestInterceptor {

    @Value("${internal.service-key:}")
    private String serviceKey;

    @Override
    public void apply(RequestTemplate template) {
        if (!serviceKey.isEmpty()) {
            template.header("X-Internal-Key", serviceKey);
        }
    }
}
