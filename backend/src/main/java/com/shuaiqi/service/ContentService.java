package com.shuaiqi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.dto.UserPublicInfo;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.dto.*;
import com.shuaiqi.entity.Content;
import com.shuaiqi.mapper.ContentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentMapper contentMapper;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;
    private final NotificationService notificationService;

    private static final String CONTENT_LIKES_KEY = "content:likes:";
    private static final String CONTENT_FAVORITES_KEY = "content:favorites:";

    private static final String LIKE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then return 0 end " +
            "redis.call('SADD', KEYS[1], ARGV[1]) return 1";

    private static final String UNLIKE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 0 then return 0 end " +
            "redis.call('SREM', KEYS[1], ARGV[1]) return 1";

    private static final String FAVORITE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then return 0 end " +
            "redis.call('SADD', KEYS[1], ARGV[1]) return 1";

    private static final String UNFAVORITE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 0 then return 0 end " +
            "redis.call('SREM', KEYS[1], ARGV[1]) return 1";

    public Page<ContentResponse> getContentList(ContentListParams params, Long currentUserId) {
        Page<Content> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Content::getStatus, 1);

        if (params.getCategoryId() != null) {
            wrapper.eq(Content::getCategoryId, params.getCategoryId());
        }
        if (params.getAuthorId() != null) {
            wrapper.eq(Content::getAuthorId, params.getAuthorId());
        }
        if (params.getKeyword() != null && !params.getKeyword().isEmpty()) {
            wrapper.and(w -> w
                    .like(Content::getTitle, params.getKeyword())
                    .or()
                    .like(Content::getSummary, params.getKeyword())
            );
        }

        switch (params.getSortBy() != null ? params.getSortBy() : "latest") {
            case "popular": wrapper.orderByDesc(Content::getLikeCount); break;
            case "hot": wrapper.orderByDesc(Content::getViewCount); break;
            default: wrapper.orderByDesc(Content::getCreateTime);
        }

        Page<Content> contentPage = contentMapper.selectPage(page, wrapper);
        List<Content> records = contentPage.getRecords();
        if (records.isEmpty()) {
            Page<ContentResponse> emptyPage = new Page<>();
            emptyPage.setCurrent(contentPage.getCurrent());
            emptyPage.setSize(contentPage.getSize());
            emptyPage.setTotal(contentPage.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        Set<String> likedContentIds = new HashSet<>();
        Set<String> favoritedContentIds = new HashSet<>();
        if (currentUserId != null) {
            try {
                String userIdStr = currentUserId.toString();
                for (Content c : records) {
                    if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(CONTENT_LIKES_KEY + c.getId(), userIdStr))) {
                        likedContentIds.add(c.getId().toString());
                    }
                    if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(CONTENT_FAVORITES_KEY + c.getId(), userIdStr))) {
                        favoritedContentIds.add(c.getId().toString());
                    }
                }
            } catch (Exception e) {
                log.warn("批量查询点赞/收藏状态失败", e);
            }
        }

        Set<Long> authorIds = records.stream().map(Content::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, UserPublicInfo> authorMap = batchGetUserInfo(new ArrayList<>(authorIds));

        final Set<String> finalLikedIds = likedContentIds;
        final Set<String> finalFavIds = favoritedContentIds;
        final Map<Long, UserPublicInfo> finalAuthorMap = authorMap;

        Page<ContentResponse> responsePage = new Page<>();
        responsePage.setCurrent(contentPage.getCurrent());
        responsePage.setSize(contentPage.getSize());
        responsePage.setTotal(contentPage.getTotal());
        responsePage.setRecords(records.stream()
                .map(c -> convertToResponse(c, currentUserId, finalLikedIds, finalFavIds, finalAuthorMap))
                .toList());
        return responsePage;
    }

    public ContentResponse getContentDetail(Long contentId, Long currentUserId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }
        contentMapper.incrementViewCount(contentId);
        content = contentMapper.selectById(contentId);
        return convertToResponse(content, currentUserId);
    }

    public ContentBriefInfo getContentBrief(Long contentId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw BusinessException.notFound("内容不存在");
        }
        return ContentBriefInfo.builder()
                .id(content.getId())
                .authorId(content.getAuthorId())
                .title(content.getTitle())
                .build();
    }

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

    @Transactional
    public void deleteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw BusinessException.notFound("内容不存在");
        }
        if (!content.getAuthorId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此内容");
        }
        content.setStatus(0);
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    @Transactional
    public void likeContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }
        String key = CONTENT_LIKES_KEY + contentId;
        String userIdStr = userId.toString();
        int rows = contentMapper.incrementLikeCount(contentId);
        if (rows == 0) {
            throw BusinessException.error("点赞失败");
        }
        try {
            redisTemplate.execute(new DefaultRedisScript<>(LIKE_LUA_SCRIPT, Long.class),
                    Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("点赞 Redis 缓存更新失败: contentId={}, userId={}", contentId, userId, e);
        }
        if (!content.getAuthorId().equals(userId)) {
            try {
                notificationService.createNotification("like", "有人赞了你的内容",
                        "你的内容被点赞了", content.getAuthorId(), contentId, "content");
            } catch (Exception e) {
                log.warn("发送点赞通知失败: contentId={}, authorId={}", contentId, content.getAuthorId(), e);
            }
        }
    }

    @Transactional
    public void unlikeContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }
        String key = CONTENT_LIKES_KEY + contentId;
        String userIdStr = userId.toString();
        contentMapper.decrementLikeCount(contentId);
        try {
            redisTemplate.execute(new DefaultRedisScript<>(UNLIKE_LUA_SCRIPT, Long.class),
                    Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("取消点赞 Redis 缓存更新失败: contentId={}, userId={}", contentId, userId, e);
        }
    }

    @Transactional
    public void favoriteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }
        String key = CONTENT_FAVORITES_KEY + contentId;
        String userIdStr = userId.toString();
        int rows = contentMapper.incrementFavoriteCount(contentId);
        if (rows == 0) {
            throw BusinessException.error("收藏失败");
        }
        try {
            redisTemplate.execute(new DefaultRedisScript<>(FAVORITE_LUA_SCRIPT, Long.class),
                    Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("收藏 Redis 缓存更新失败: contentId={}, userId={}", contentId, userId, e);
        }
        if (!content.getAuthorId().equals(userId)) {
            try {
                notificationService.createNotification("favorite", "有人收藏了你的内容",
                        "你的内容被收藏了", content.getAuthorId(), contentId, "content");
            } catch (Exception e) {
                log.warn("发送收藏通知失败: contentId={}, authorId={}", contentId, content.getAuthorId(), e);
            }
        }
    }

    @Transactional
    public void unfavoriteContent(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != 1) {
            throw BusinessException.notFound("内容不存在");
        }
        String key = CONTENT_FAVORITES_KEY + contentId;
        String userIdStr = userId.toString();
        contentMapper.decrementFavoriteCount(contentId);
        try {
            redisTemplate.execute(new DefaultRedisScript<>(UNFAVORITE_LUA_SCRIPT, Long.class),
                    Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("取消收藏 Redis 缓存更新失败: contentId={}, userId={}", contentId, userId, e);
        }
    }

    public Page<ContentResponse> getRecommendContent(Integer page, Integer pageSize, Long currentUserId) {
        ContentListParams params = new ContentListParams();
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setSortBy("popular");
        return getContentList(params, currentUserId);
    }

    public Page<ContentResponse> getHotContent(Integer page, Integer pageSize, Long currentUserId) {
        ContentListParams params = new ContentListParams();
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setSortBy("hot");
        return getContentList(params, currentUserId);
    }

    public void updateCommentCount(Long contentId, Integer increment) {
        int rows = contentMapper.updateCommentCount(contentId, increment);
        if (rows == 0) {
            throw BusinessException.notFound("内容不存在");
        }
    }

    public Long getContentCount() {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Content::getStatus, 1);
        return contentMapper.selectCount(wrapper);
    }

    public Long getTotalLikes() {
        return contentMapper.sumLikeCount();
    }

    private Map<Long, UserPublicInfo> batchGetUserInfo(List<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        Map<Long, UserPublicInfo> map = new HashMap<>();
        for (Long uid : userIds) {
            try {
                UserPublicInfo info = userService.getUserPublicInfo(uid);
                if (info != null) {
                    map.put(uid, info);
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: userId={}", uid, e);
            }
        }
        return map;
    }

    private ContentResponse convertToResponse(Content content, Long currentUserId,
                                               Set<String> likedIds, Set<String> favIds,
                                               Map<Long, UserPublicInfo> authorMap) {
        boolean isLiked = likedIds.contains(content.getId().toString());
        boolean isFavorited = favIds.contains(content.getId().toString());

        ContentResponse.AuthorInfo authorInfo = null;
        if (content.getAuthorId() != null) {
            UserPublicInfo userInfo = authorMap.get(content.getAuthorId());
            authorInfo = ContentResponse.AuthorInfo.builder()
                    .id(content.getAuthorId())
                    .username(userInfo != null ? userInfo.getUsername() : "未知用户")
                    .avatar(userInfo != null ? userInfo.getAvatar() : null)
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

    private ContentResponse convertToResponse(Content content, Long currentUserId) {
        Set<String> likedIds = new HashSet<>();
        Set<String> favIds = new HashSet<>();
        if (currentUserId != null) {
            String userIdStr = currentUserId.toString();
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(CONTENT_LIKES_KEY + content.getId(), userIdStr))) {
                likedIds.add(content.getId().toString());
            }
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(CONTENT_FAVORITES_KEY + content.getId(), userIdStr))) {
                favIds.add(content.getId().toString());
            }
        }
        Map<Long, UserPublicInfo> authorMap = new HashMap<>();
        if (content.getAuthorId() != null) {
            try {
                UserPublicInfo info = userService.getUserPublicInfo(content.getAuthorId());
                if (info != null) {
                    authorMap.put(content.getAuthorId(), info);
                }
            } catch (Exception e) {
                log.warn("获取作者信息失败: authorId={}", content.getAuthorId(), e);
            }
        }
        return convertToResponse(content, currentUserId, likedIds, favIds, authorMap);
    }
}
