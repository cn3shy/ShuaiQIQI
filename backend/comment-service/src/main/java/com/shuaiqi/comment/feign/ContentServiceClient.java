package com.shuaiqi.comment.feign;

import com.shuaiqi.comment.dto.ContentBriefInfo;
import com.shuaiqi.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "content-service", path = "/api/content", fallback = ContentServiceFallback.class)
public interface ContentServiceClient {

    @GetMapping("/{contentId}/brief")
    Result<ContentBriefInfo> getContentBrief(@PathVariable("contentId") Long contentId);

    @PostMapping("/{contentId}/comment-count")
    Result<Void> updateCommentCount(@PathVariable("contentId") Long contentId,
                                    @RequestParam("increment") Integer increment);
}
