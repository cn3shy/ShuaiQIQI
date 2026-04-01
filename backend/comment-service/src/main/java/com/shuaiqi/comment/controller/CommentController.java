package com.shuaiqi.comment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.common.result.Result;
import com.shuaiqi.comment.dto.CommentResponse;
import com.shuaiqi.comment.dto.CreateCommentRequest;
import com.shuaiqi.comment.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取内容的评论列表
     */
    @GetMapping("/content/{contentId}")
    public Result<Page<CommentResponse>> getCommentsByContentId(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long currentUserId = getUserIdFromRequest(request);
        Page<CommentResponse> comments = commentService.getCommentsByContentId(contentId, page, pageSize, currentUserId);
        return Result.success(comments);
    }

    /**
     * 创建评论
     */
    @PostMapping("/create")
    public Result<CommentResponse> createComment(
            @Validated @RequestBody CreateCommentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        CommentResponse comment = commentService.createComment(request, userId);
        return Result.success("评论成功", comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        commentService.deleteComment(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        commentService.likeComment(id, userId);
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        commentService.unlikeComment(id, userId);
        return Result.success("取消点赞成功", null);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            throw BusinessException.unauthorized("请先登录");
        }
        return Long.parseLong(userId);
    }
}
