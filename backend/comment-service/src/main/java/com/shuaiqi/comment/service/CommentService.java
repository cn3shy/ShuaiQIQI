package com.shuaiqi.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.comment.dto.CommentResponse;
import com.shuaiqi.comment.dto.CreateCommentRequest;
import com.shuaiqi.comment.entity.Comment;
import com.shuaiqi.comment.feign.ContentServiceClient;
import com.shuaiqi.comment.feign.NotificationServiceClient;
import com.shuaiqi.comment.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final StringRedisTemplate redisTemplate;
    private final ContentServiceClient contentServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    private static final String COMMENT_LIKES_KEY = "comment:likes:";

    /**
     * 评论点赞 Lua 脚本
     */
    private static final String COMMENT_LIKE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then " +
            "    return 0 " +
            "end " +
            "redis.call('SADD', KEYS[1], ARGV[1]) " +
            "return 1";

    /**
     * 评论取消点赞 Lua 脚本
     */
    private static final String COMMENT_UNLIKE_LUA_SCRIPT =
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 0 then " +
            "    return 0 " +
            "end " +
            "redis.call('SREM', KEYS[1], ARGV[1]) " +
            "return 1";

    /**
     * 获取内容的评论列表
     */
    public Page<CommentResponse> getCommentsByContentId(Long contentId, Integer page, Integer pageSize, Long currentUserId) {
        Page<Comment> commentPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getContentId, contentId)
                .eq(Comment::getStatus, 1)
                .isNull(Comment::getParentId)  // 只获取顶级评论
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> result = commentMapper.selectPage(commentPage, wrapper);

        // 转换为响应对象
        Page<CommentResponse> responsePage = new Page<>();
        responsePage.setCurrent(result.getCurrent());
        responsePage.setSize(result.getSize());
        responsePage.setTotal(result.getTotal());

        List<CommentResponse> comments = result.getRecords().stream()
                .map(comment -> {
                    CommentResponse response = convertToResponse(comment, currentUserId);
                    // 获取子评论
                    List<CommentResponse> children = getChildComments(comment.getId(), currentUserId);
                    response.setChildren(children);
                    return response;
                })
                .toList();

        responsePage.setRecords(comments);
        return responsePage;
    }

    /**
     * 创建评论
     */
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, Long userId) {
        Long contentAuthorId = null;
        try {
            var contentResult = contentServiceClient.getContentBrief(request.getContentId());
            if (contentResult != null && contentResult.getData() != null) {
                contentAuthorId = contentResult.getData().getAuthorId();
            }
        } catch (Exception e) {
            throw BusinessException.notFound("内容不存在，无法评论");
        }

        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null || parentComment.getStatus() != 1) {
                throw BusinessException.notFound("回复的评论不存在");
            }
            if (!parentComment.getContentId().equals(request.getContentId())) {
                throw BusinessException.badRequest("父评论不属于该内容");
            }
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setContentId(request.getContentId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        commentMapper.insert(comment);

        try {
            contentServiceClient.updateCommentCount(request.getContentId(), 1);
        } catch (Exception e) {
            log.error("更新内容评论数失败，已记录补偿日志: contentId={}, commentId={}", request.getContentId(), comment.getId(), e);
        }

        if (contentAuthorId != null && !contentAuthorId.equals(userId)) {
            try {
                notificationServiceClient.createNotification("comment", "有人评论了你的内容",
                        request.getContent().length() > 50 ? request.getContent().substring(0, 50) + "..." : request.getContent(),
                        contentAuthorId, comment.getId(), "comment");
            } catch (Exception e) {
                log.warn("发送评论通知失败: contentId={}, authorId={}", request.getContentId(), contentAuthorId, e);
            }
        }

        return convertToResponse(comment, userId);
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw BusinessException.notFound("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此评论");
        }

        // 软删除
        comment.setStatus(0);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);

        // 更新内容评论数（失败时记录日志，后续可通过定时任务补偿）
        try {
            contentServiceClient.updateCommentCount(comment.getContentId(), -1);
        } catch (Exception e) {
            log.error("更新内容评论数失败，已记录补偿日志: contentId={}, commentId={}", comment.getContentId(), commentId, e);
        }
    }

    /**
     * 点赞评论（DB 优先，Redis 作为缓存）
     */
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 1) {
            throw BusinessException.notFound("评论不存在");
        }

        String key = COMMENT_LIKES_KEY + commentId;
        String userIdStr = userId.toString();

        // 先更新 DB
        int rows = commentMapper.incrementLikeCount(commentId);
        if (rows == 0) {
            throw BusinessException.error("点赞失败");
        }

        // 再更新 Redis
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(COMMENT_LIKE_LUA_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("评论点赞 Redis 缓存更新失败: commentId={}, userId={}", commentId, userId, e);
        }
    }

    /**
     * 取消点赞评论（DB 优先，Redis 作为缓存）
     */
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 1) {
            throw BusinessException.notFound("评论不存在");
        }

        String key = COMMENT_LIKES_KEY + commentId;
        String userIdStr = userId.toString();

        // 先更新 DB
        commentMapper.decrementLikeCount(commentId);

        // 再更新 Redis
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(COMMENT_UNLIKE_LUA_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), userIdStr);
        } catch (Exception e) {
            log.warn("评论取消点赞 Redis 缓存更新失败: commentId={}, userId={}", commentId, userId, e);
        }
    }

    /**
     * 获取评论总数
     */
    public Long getCommentCount() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getStatus, 1);
        return commentMapper.selectCount(wrapper);
    }

    /**
     * 获取子评论
     */
    private List<CommentResponse> getChildComments(Long parentId, Long currentUserId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime);

        List<Comment> children = commentMapper.selectList(wrapper);
        return children.stream()
                .map(comment -> convertToResponse(comment, currentUserId))
                .toList();
    }

    /**
     * 转换为响应对象
     */
    private CommentResponse convertToResponse(Comment comment, Long currentUserId) {
        boolean isLiked = false;
        if (currentUserId != null) {
            String key = COMMENT_LIKES_KEY + comment.getId();
            isLiked = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, currentUserId.toString()));
        }

        CommentResponse.UserInfo userInfo = null;
        if (comment.getUserId() != null) {
            userInfo = CommentResponse.UserInfo.builder()
                    .id(comment.getUserId())
                    .username(comment.getUserName() != null ? comment.getUserName() : "未知用户")
                    .avatar(comment.getUserAvatar())
                    .build();
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .contentId(comment.getContentId())
                .parentId(comment.getParentId())
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .user(userInfo)
                .createTime(comment.getCreateTime())
                .build();
    }
}
