package com.shuaiqi.controller;

import com.shuaiqi.common.result.Result;
import com.shuaiqi.common.utils.RequestUtils;
import com.shuaiqi.dto.NotificationListResponse;
import com.shuaiqi.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/create")
    public Result<Void> createNotification(
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam String targetType) {
        notificationService.createNotification(type, title, content, userId, targetId, targetType);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<NotificationListResponse> getNotificationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        NotificationListResponse notificationList = notificationService.getNotificationList(userId, page, pageSize);
        return Result.success(notificationList);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        notificationService.markAsRead(id, userId);
        return Result.success("标记成功", null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        notificationService.markAllAsRead(userId);
        return Result.success("标记成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        notificationService.deleteNotification(id, userId);
        return Result.success("删除成功", null);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }
}
