package com.shuaiqi.notification.controller;

import com.shuaiqi.common.result.Result;
import com.shuaiqi.notification.dto.NotificationListResponse;
import com.shuaiqi.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    public Result<NotificationListResponse> getNotificationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        NotificationListResponse notificationList = notificationService.getNotificationList(userId, page, pageSize);
        return Result.success(notificationList);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        notificationService.markAsRead(id, userId);
        return Result.success("标记成功", null);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        notificationService.markAllAsRead(userId);
        return Result.success("标记成功", null);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        notificationService.deleteNotification(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            throw new RuntimeException("未授权访问");
        }
        return Long.parseLong(userId);
    }
}
