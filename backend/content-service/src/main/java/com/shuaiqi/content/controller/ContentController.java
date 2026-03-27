package com.shuaiqi.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.result.Result;
import com.shuaiqi.content.dto.*;
import com.shuaiqi.content.entity.Category;
import com.shuaiqi.content.service.CategoryService;
import com.shuaiqi.content.service.ContentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容控制器
 */
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final CategoryService categoryService;

    /**
     * 获取内容列表
     */
    @GetMapping("/list")
    public Result<Page<ContentResponse>> getContentList(
            @ModelAttribute ContentListParams params,
            HttpServletRequest request) {
        Long currentUserId = getUserIdFromRequest(request);
        Page<ContentResponse> contentList = contentService.getContentList(params, currentUserId);
        return Result.success(contentList);
    }

    /**
     * 获取内容详情
     */
    @GetMapping("/{id}")
    public Result<ContentResponse> getContentDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long currentUserId = getUserIdFromRequest(request);
        ContentResponse content = contentService.getContentDetail(id, currentUserId);
        return Result.success(content);
    }

    /**
     * 创建内容
     */
    @PostMapping("/create")
    public Result<ContentResponse> createContent(
            @Validated @RequestBody CreateContentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        ContentResponse content = contentService.createContent(request, userId);
        return Result.success("创建成功", content);
    }

    /**
     * 更新内容
     */
    @PutMapping("/{id}")
    public Result<ContentResponse> updateContent(
            @PathVariable Long id,
            @Validated @RequestBody CreateContentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        ContentResponse content = contentService.updateContent(id, request, userId);
        return Result.success("更新成功", content);
    }

    /**
     * 删除内容
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        contentService.deleteContent(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 点赞内容
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        contentService.likeContent(id, userId);
        return Result.success("点赞成功", null);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        contentService.unlikeContent(id, userId);
        return Result.success("取消点赞成功", null);
    }

    /**
     * 收藏内容
     */
    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        contentService.favoriteContent(id, userId);
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        contentService.unfavoriteContent(id, userId);
        return Result.success("取消收藏成功", null);
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public Result<List<Category>> getCategoryList() {
        List<Category> categories = categoryService.getCategoryList();
        return Result.success(categories);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/category/{id}")
    public Result<Category> getCategoryDetail(@PathVariable Long id) {
        Category category = categoryService.getCategoryDetail(id);
        return Result.success(category);
    }

    /**
     * 获取推荐内容
     */
    @GetMapping("/recommend")
    public Result<Page<ContentResponse>> getRecommendContent(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long currentUserId = getUserIdFromRequest(request);
        Page<ContentResponse> contentList = contentService.getRecommendContent(page, pageSize, currentUserId);
        return Result.success(contentList);
    }

    /**
     * 获取热门内容
     */
    @GetMapping("/hot")
    public Result<Page<ContentResponse>> getHotContent(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long currentUserId = getUserIdFromRequest(request);
        Page<ContentResponse> contentList = contentService.getHotContent(page, pageSize, currentUserId);
        return Result.success(contentList);
    }

    /**
     * 更新评论数（服务间调用）
     */
    @PostMapping("/{contentId}/comment-count")
    public Result<Void> updateCommentCount(
            @PathVariable Long contentId,
            @RequestParam Integer increment) {
        contentService.updateCommentCount(contentId, increment);
        return Result.success(null);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            return null; // 未登录用户
        }
        return Long.parseLong(userId);
    }
}
