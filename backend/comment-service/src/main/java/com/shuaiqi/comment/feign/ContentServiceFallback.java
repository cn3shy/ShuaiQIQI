package com.shuaiqi.comment.feign;

import com.shuaiqi.comment.dto.ContentBriefInfo;
import com.shuaiqi.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContentServiceFallback implements ContentServiceClient {

    @Override
    public Result<ContentBriefInfo> getContentBrief(Long contentId) {
        log.warn("内容服务不可用，降级处理: contentId={}", contentId);
        return Result.error("内容服务暂时不可用");
    }

    @Override
    public Result<Void> updateCommentCount(Long contentId, Integer increment) {
        log.warn("内容服务不可用，更新评论数降级: contentId={}, increment={}", contentId, increment);
        return Result.error("内容服务暂时不可用");
    }
}
