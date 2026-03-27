package com.shuaiqi.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 内容服务 Feign 客户端
 */
@FeignClient(name = "content-service", path = "/api/content")
public interface ContentServiceClient {

    /**
     * 更新评论数
     */
    @PostMapping("/{contentId}/comment-count")
    void updateCommentCount(@PathVariable("contentId") Long contentId,
                           @RequestParam("increment") Integer increment);
}
