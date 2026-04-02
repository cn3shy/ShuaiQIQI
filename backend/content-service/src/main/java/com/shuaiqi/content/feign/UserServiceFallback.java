package com.shuaiqi.content.feign;

import com.shuaiqi.common.dto.UserPublicInfo;
import com.shuaiqi.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceFallback implements UserServiceClient {

    @Override
    public Result<UserPublicInfo> getUserDetail(Long userId) {
        log.warn("用户服务不可用，降级处理: userId={}", userId);
        return Result.error("用户服务暂时不可用");
    }
}
