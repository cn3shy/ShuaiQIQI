package com.shuaiqi.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.content.dto.*;
import com.shuaiqi.content.entity.Content;
import com.shuaiqi.content.feign.UserServiceClient;
import com.shuaiqi.content.mapper.ContentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
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
    private final UserServiceClient userServiceClient;

    private static final String CONTENT_LIKES_KEY = "content:likes:";
    private static final String CONTENT_FAVORITES_KEY = "content:favorites:";

    /**
     * 点赞 Lua 脚本：原子性检查并添加用户到集合，返回 1=成功 0=已点赞
     */
    private static final String LIKE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then " +
            "    return 0 " +
            "end " +
            "redis.call('SADD', KEYS[1], ARGV[1]) " +
            "return 1";

    /**
     * 取消点赞 Lua 脚本：原子性检查并移除用户，返回 1=成功 0=未点赞
     */
    private static final String UNLIKE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 0 then " +
            "    return 0 " +
            "end " +
            "redis.call('SREM', KEYS[1], ARGV[1]) " +
            "return 1";

    /**
     * 收藏 Lua 脚本：原子性检查并添加用户到集合，返回 1=成功 0=已收藏
     */
    private static final String FAVORITE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then " +
            "    return 0 " +
            "end " +
            "redis.call('SADD', KEYS[1], ARGV[1]) " +
            "return 1";

    /**
     * 取消收藏 Lua 脚本：原子性检查并移除用户，返回 1=成功 0=未收藏
     */
    private static final String UNFAVORITE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 0 then " +
            "    return 0 " +
            "end " +
            "redis.call('SREM', KEYS[1], ARGV[1]) " +
            "return 1";

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

        if (params.getAuthorId() != null) {
            wrapper.eq(Content::getAuthorId, params.getAuthorId());
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
        // 使用原子 SQL 递增浏览量
        contentMapper.incrementViewCount(contentId);
        // 重新查询获取最新数据（或使用 Redis 缓存）
        content = contentMapper.selectById(contentId);
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
        if (content == null || content.getStatus() != 1) {
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
     * 点赞内容（DB 优先，Redis 作为缓存，保证数据一致性）
     */
    @Transactional
    public void likeContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_LIKES_KEY + contentId;
        String userIdStr = userId.toString();

        // 先更新 DB（原子操作），保证数据一致性
        int rows = contentMapper.incrementLikeCount(contentId);
        if (rows == 0) {
            throw BusinessException.error("点赞失败");
        }

        // DB 成功后再更新 Redis，Redis 失败不影响主流程
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(LIKE_LUA_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("点赞 Redis 缓存更新失败，不影响主流程: contentId={}, userId={}", contentId, userId, e);
        }
    }

    /**
     * 取消点赞（DB 优先，Redis 作为缓存）
     */
    @Transactional
    public void unlikeContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_LIKES_KEY + contentId;
        String userIdStr = userId.toString();

        // 先更新 DB
        contentMapper.decrementLikeCount(contentId);

        // 再更新 Redis
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLIKE_LUA_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("取消点赞 Redis 缓存更新失败，不影响主流程: contentId={}, userId={}", contentId, userId, e);
        }
    }

    /**
     * 收藏内容（DB 优先，Redis 作为缓存）
     */
    @Transactional
    public void favoriteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_FAVORITES_KEY + contentId;
        String userIdStr = userId.toString();

        // 先更新 DB
        int rows = contentMapper.incrementFavoriteCount(contentId);
        if (rows == 0) {
            throw BusinessException.error("收藏失败");
        }

        // 再更新 Redis
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(FAVORITE_LUA_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("收藏 Redis 缓存更新失败，不影响主流程: contentId={}, userId={}", contentId, userId, e);
        }
    }

    /**
     * 取消收藏（DB 优先，Redis 作为缓存）
     */
    @Transactional
    public void unfavoriteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }

        String key = CONTENT_FAVORITES_KEY + contentId;
        String userIdStr = userId.toString();

        // 先更新 DB
        contentMapper.decrementFavoriteCount(contentId);

        // 再更新 Redis
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNFAVORITE_LUA_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("取消收藏 Redis 缓存更新失败，不影响主流程: contentId={}, userId={}", contentId, userId, e);
        }
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
     * 更新评论数（使用原子 SQL 防止丢失更新）
     */
    public void updateCommentCount(Long contentId, Integer increment) {
        int rows = contentMapper.updateCommentCount(contentId, increment);
        if (rows == 0) {
            throw BusinessException.notFound("内容不存在");
        }
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
            String authorName = content.getAuthorName();
            String authorAvatar = content.getAuthorAvatar();

            // 如果作者信息未填充，通过 Feign 获取
            if (authorName == null) {
                try {
                    Map<String, Object> userDetail = userServiceClient.getUserDetail(content.getAuthorId());
                    if (userDetail != null && userDetail.containsKey("data")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) userDetail.get("data");
                        authorName = (String) userData.get("username");
                        authorAvatar = (String) userData.get("avatar");
                    }
                } catch (Exception e) {
                    log.warn("获取作者信息失败: authorId={}", content.getAuthorId(), e);
                    authorName = "未知用户";
                }
            }

            authorInfo = ContentResponse.AuthorInfo.builder()
                    .id(content.getAuthorId())
                    .username(authorName != null ? authorName : "未知用户")
                    .avatar(authorAvatar)
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
