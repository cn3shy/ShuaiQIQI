package com.shuaiqi.user.feign;

import com.shuaiqi.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", path = "/api/notification", fallback = NotificationServiceFallback.class)
public interface NotificationServiceClient {

    @PostMapping("/create")
    Result<Void> createNotification(
            @RequestParam("type") String type,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("userId") Long userId,
            @RequestParam("targetId") Long targetId,
            @RequestParam("targetType") String targetType);
}
