package com.shuaiqi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.result.Result;
import com.shuaiqi.common.utils.RequestUtils;
import com.shuaiqi.dto.CommentResponse;
import com.shuaiqi.dto.CreateCommentRequest;
import com.shuaiqi.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/content/{contentId}")
    public Result<Page<CommentResponse>> getCommentsByContentId(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long currentUserId = RequestUtils.getUserIdFromRequestOrNull(request);
        Page<CommentResponse> comments = commentService.getCommentsByContentId(contentId, page, pageSize, currentUserId);
        return Result.success(comments);
    }

    @PostMapping("/create")
    public Result<CommentResponse> createComment(
            @Validated @RequestBody CreateCommentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = RequestUtils.getUserIdFromRequest(httpRequest);
        CommentResponse comment = commentService.createComment(request, userId);
        return Result.success("评论成功", comment);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        commentService.deleteComment(id, userId);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        commentService.likeComment(id, userId);
        return Result.success("点赞成功", null);
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        commentService.unlikeComment(id, userId);
        return Result.success("取消点赞成功", null);
    }

    @GetMapping("/count")
    public Result<Long> getCommentCount() {
        Long count = commentService.getCommentCount();
        return Result.success(count);
    }
}
