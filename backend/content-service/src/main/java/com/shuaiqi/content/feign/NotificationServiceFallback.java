package com.shuaiqi.content.feign;

import com.shuaiqi.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationServiceFallback implements NotificationServiceClient {

    @Override
    public Result<Void> createNotification(String type, String title, String content,
                                           Long userId, Long targetId, String targetType) {
        log.warn("通知服务不可用，降级处理: type={}, userId={}, targetId={}", type, userId, targetId);
        return Result.error("通知服务暂时不可用");
    }
}
