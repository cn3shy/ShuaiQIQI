package com.shuaiqi.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.content.dto.*;
import com.shuaiqi.content.entity.Content;
import com.shuaiqi.content.mapper.ContentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 内容服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentMapper contentMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String CONTENT_LIKES_KEY = "content:likes:";
    private static final String CONTENT_FAVORITES_KEY = "content:favorites:";

    /**
     * 获取内容列表
     */
    public Page<ContentResponse> getContentList(ContentListParams params, Long currentUserId) {
        Page<Content> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();

        // 筛选条件
        wrapper.eq(Content::getStatus, 1);

        if (params.getCategoryId() != null) {
            wrapper.eq(Content::getCategoryId, params.getCategoryId());
        }

        if (params.getKeyword() != null && !params.getKeyword().isEmpty()) {
            // 支持标题和内容的模糊搜索
            wrapper.and(w -> w
                    .like(Content::getTitle, params.getKeyword())
                    .or()
                    .like(Content::getSummary, params.getKeyword())
            );
        }

        // 排序
        switch (params.getSortBy()) {
            case "popular":
                wrapper.orderByDesc(Content::getLikeCount);
                break;
            case "hot":
                wrapper.orderByDesc(Content::getViewCount);
                break;
            default:
                wrapper.orderByDesc(Content::getCreateTime);
        }

        Page<Content> contentPage = contentMapper.selectPage(page, wrapper);

        // 转换为响应对象
        Page<ContentResponse> responsePage = new Page<>();
        responsePage.setCurrent(contentPage.getCurrent());
        responsePage.setSize(contentPage.getSize());
        responsePage.setTotal(contentPage.getTotal());

        responsePage.setRecords(contentPage.getRecords().stream()
                .map(content -> convertToResponse(content, currentUserId))
                .toList());

        return responsePage;
    }

    /**
     * 获取内容详情
     */
    public ContentResponse getContentDetail(Long contentId, Long currentUserId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }
        // 递增浏览量
        content.setViewCount(content.getViewCount() + 1);
        contentMapper.updateById(content);
        return convertToResponse(content, currentUserId);
    }

    /**
     * 创建内容
     */
    @Transactional
    public ContentResponse createContent(CreateContentRequest request, Long authorId) {
        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setSummary(request.getSummary());
        content.setContent(request.getContent());
        content.setCoverImage(request.getCoverImage());
        content.setAuthorId(authorId);
        content.setCategoryId(request.getCategoryId());
        content.setLikeCount(0);
        content.setFavoriteCount(0);
        content.setCommentCount(0);
        content.setStatus(1);
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());

        contentMapper.insert(content);

        return convertToResponse(content, authorId);
    }

    /**
     * 更新内容
     */
    @Transactional
    public ContentResponse updateContent(Long contentId, CreateContentRequest request, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw BusinessException.notFound("内容不存在");
        }
        if (!content.getAuthorId().equals(userId)) {
            throw BusinessException.forbidden("无权修改此内容");
        }

        content.setTitle(request.getTitle());
        content.setSummary(request.getSummary());
        content.setContent(request.getContent());
        content.setCoverImage(request.getCoverImage());
        content.setCategoryId(request.getCategoryId());
        content.setUpdateTime(LocalDateTime.now());

        contentMapper.updateById(content);

        return convertToResponse(content, userId);
    }

    /**
     * 删除内容
     */
    @Transactional
    public void deleteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw BusinessException.notFound("内容不存在");
        }
        if (!content.getAuthorId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此内容");
        }

        // 软删除
        content.setStatus(0);
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    /**
     * 点赞内容
     */
    public void likeContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_LIKES_KEY + contentId;
        String userIdStr = userId.toString();

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userIdStr))) {
            throw BusinessException.badRequest("已经点赞过了");
        }

        redisTemplate.opsForSet().add(key, userIdStr);
        content.setLikeCount(content.getLikeCount() + 1);
        contentMapper.updateById(content);
    }

    /**
     * 取消点赞
     */
    public void unlikeContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_LIKES_KEY + contentId;
        String userIdStr = userId.toString();

        if (!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userIdStr))) {
            throw BusinessException.badRequest("还没有点赞");
        }

        redisTemplate.opsForSet().remove(key, userIdStr);
        content.setLikeCount(Math.max(0, content.getLikeCount() - 1));
        contentMapper.updateById(content);
    }

    /**
     * 收藏内容
     */
    public void favoriteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_FAVORITES_KEY + contentId;
        String userIdStr = userId.toString();

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userIdStr))) {
            throw BusinessException.badRequest("已经收藏过了");
        }

        redisTemplate.opsForSet().add(key, userIdStr);
        content.setFavoriteCount(content.getFavoriteCount() + 1);
        contentMapper.updateById(content);
    }

    /**
     * 取消收藏
     */
    public void unfavoriteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_FAVORITES_KEY + contentId;
        String userIdStr = userId.toString();

        if (!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userIdStr))) {
            throw BusinessException.badRequest("还没有收藏");
        }

        redisTemplate.opsForSet().remove(key, userIdStr);
        content.setFavoriteCount(Math.max(0, content.getFavoriteCount() - 1));
        contentMapper.updateById(content);
    }

    /**
     * 获取推荐内容
     */
    public Page<ContentResponse> getRecommendContent(Integer page, Integer pageSize, Long currentUserId) {
        ContentListParams params = new ContentListParams();
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setSortBy("popular");
        return getContentList(params, currentUserId);
    }

    /**
     * 获取热门内容
     */
    public Page<ContentResponse> getHotContent(Integer page, Integer pageSize, Long currentUserId) {
        ContentListParams params = new ContentListParams();
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setSortBy("hot");
        return getContentList(params, currentUserId);
    }

    /**
     * 更新评论数（服务间调用）
     */
    @Transactional
    public void updateCommentCount(Long contentId, Integer increment) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw BusinessException.notFound("内容不存在");
        }
        content.setCommentCount(Math.max(0, content.getCommentCount() + increment));
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    /**
     * 转换为响应对象
     */
    private ContentResponse convertToResponse(Content content, Long currentUserId) {
        // 检查当前用户是否点赞和收藏
        boolean isLiked = false;
        boolean isFavorited = false;

        if (currentUserId != null) {
            String likeKey = CONTENT_LIKES_KEY + content.getId();
            String favoriteKey = CONTENT_FAVORITES_KEY + content.getId();
            isLiked = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, currentUserId.toString()));
            isFavorited = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(favoriteKey, currentUserId.toString()));
        }

        ContentResponse.AuthorInfo authorInfo = null;
        if (content.getAuthorId() != null) {
            authorInfo = ContentResponse.AuthorInfo.builder()
                    .id(content.getAuthorId())
                    .username(content.getAuthorName() != null ? content.getAuthorName() : "未知用户")
                    .avatar(content.getAuthorAvatar())
                    .build();
        }

        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .summary(content.getSummary())
                .content(content.getContent())
                .coverImage(content.getCoverImage())
                .categoryId(content.getCategoryId())
                .likeCount(content.getLikeCount())
                .favoriteCount(content.getFavoriteCount())
                .commentCount(content.getCommentCount())
                .isLiked(isLiked)
                .isFavorited(isFavorited)
                .author(authorInfo)
                .createTime(content.getCreateTime())
                .updateTime(content.getUpdateTime())
                .build();
    }
}
