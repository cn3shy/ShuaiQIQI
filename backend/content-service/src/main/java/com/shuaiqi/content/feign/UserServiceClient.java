package com.shuaiqi.content.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(name = "user-service", path = "/api/user")
public interface UserServiceClient {

    /**
     * 获取用户公开信息
     */
    @GetMapping("/{userId}")
    Map<String, Object> getUserDetail(@PathVariable("userId") Long userId);
}
