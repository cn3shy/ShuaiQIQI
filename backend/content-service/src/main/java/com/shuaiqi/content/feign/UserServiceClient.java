package com.shuaiqi.content.feign;

import com.shuaiqi.common.dto.UserPublicInfo;
import com.shuaiqi.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/user", fallback = UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/{userId}")
    Result<UserPublicInfo> getUserDetail(@PathVariable("userId") Long userId);
}
