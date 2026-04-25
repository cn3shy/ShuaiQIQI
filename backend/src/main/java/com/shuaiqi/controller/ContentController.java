package com.shuaiqi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.result.Result;
import com.shuaiqi.common.utils.RequestUtils;
import com.shuaiqi.dto.*;
import com.shuaiqi.entity.Category;
import com.shuaiqi.service.CategoryService;
import com.shuaiqi.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final CategoryService categoryService;

    @Operation(summary = "获取内容列表")
    @GetMapping("/list")
    public Result<Page<ContentResponse>> getContentList(
            @ModelAttribute ContentListParams params,
            HttpServletRequest request) {
        Long currentUserId = RequestUtils.getUserIdFromRequestOrNull(request);
        Page<ContentResponse> contentList = contentService.getContentList(params, currentUserId);
        return Result.success(contentList);
    }

    @Operation(summary = "获取我的内容")
    @GetMapping("/my")
    public Result<Page<ContentResponse>> getMyContentList(
            @ModelAttribute ContentListParams params,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        params.setAuthorId(userId);
        Page<ContentResponse> contentList = contentService.getContentList(params, userId);
        return Result.success(contentList);
    }

    @Operation(summary = "获取内容详情")
    @GetMapping("/{id}")
    public Result<ContentResponse> getContentDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long currentUserId = RequestUtils.getUserIdFromRequestOrNull(request);
        ContentResponse content = contentService.getContentDetail(id, currentUserId);
        return Result.success(content);
    }

    @GetMapping("/{id}/brief")
    public Result<ContentBriefInfo> getContentBrief(@PathVariable Long id) {
        ContentBriefInfo brief = contentService.getContentBrief(id);
        return Result.success(brief);
    }

    @Operation(summary = "创建内容")
    @PostMapping("/create")
    public Result<ContentResponse> createContent(
            @Validated @RequestBody CreateContentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = RequestUtils.getUserIdFromRequest(httpRequest);
        ContentResponse content = contentService.createContent(request, userId);
        return Result.success("创建成功", content);
    }

    @Operation(summary = "更新内容")
    @PutMapping("/{id}")
    public Result<ContentResponse> updateContent(
            @PathVariable Long id,
            @Validated @RequestBody CreateContentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = RequestUtils.getUserIdFromRequest(httpRequest);
        ContentResponse content = contentService.updateContent(id, request, userId);
        return Result.success("更新成功", content);
    }

    @Operation(summary = "删除内容")
    @DeleteMapping("/{id}")
    public Result<Void> deleteContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        contentService.deleteContent(id, userId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "点赞内容")
    @PostMapping("/{id}/like")
    public Result<Void> likeContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        contentService.likeContent(id, userId);
        return Result.success("点赞成功", null);
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        contentService.unlikeContent(id, userId);
        return Result.success("取消点赞成功", null);
    }

    @Operation(summary = "收藏内容")
    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        contentService.favoriteContent(id, userId);
        return Result.success("收藏成功", null);
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteContent(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        contentService.unfavoriteContent(id, userId);
        return Result.success("取消收藏成功", null);
    }

    @Operation(summary = "获取分类列表")
    @GetMapping("/categories")
    public Result<List<Category>> getCategoryList() {
        List<Category> categories = categoryService.getCategoryList();
        return Result.success(categories);
    }

    @GetMapping("/category/{id}")
    public Result<Category> getCategoryDetail(@PathVariable Long id) {
        Category category = categoryService.getCategoryDetail(id);
        return Result.success(category);
    }

    @Operation(summary = "获取推荐内容")
    @GetMapping("/recommend")
    public Result<Page<ContentResponse>> getRecommendContent(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long currentUserId = RequestUtils.getUserIdFromRequestOrNull(request);
        Page<ContentResponse> contentList = contentService.getRecommendContent(page, pageSize, currentUserId);
        return Result.success(contentList);
    }

    @Operation(summary = "获取热门内容")
    @GetMapping("/hot")
    public Result<Page<ContentResponse>> getHotContent(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long currentUserId = RequestUtils.getUserIdFromRequestOrNull(request);
        Page<ContentResponse> contentList = contentService.getHotContent(page, pageSize, currentUserId);
        return Result.success(contentList);
    }

    @PostMapping("/{contentId}/comment-count")
    public Result<Void> updateCommentCount(
            @PathVariable Long contentId,
            @RequestParam Integer increment) {
        contentService.updateCommentCount(contentId, increment);
        return Result.success(null);
    }

    @GetMapping("/count")
    public Result<Long> getContentCount() {
        Long count = contentService.getContentCount();
        return Result.success(count);
    }

    @GetMapping("/likes/count")
    public Result<Long> getTotalLikes() {
        Long count = contentService.getTotalLikes();
        return Result.success(count);
    }
}
