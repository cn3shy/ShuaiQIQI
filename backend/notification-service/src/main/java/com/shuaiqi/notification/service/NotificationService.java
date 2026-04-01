package com.shuaiqi.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.notification.dto.NotificationListResponse;
import com.shuaiqi.notification.dto.NotificationResponse;
import com.shuaiqi.notification.entity.Notification;
import com.shuaiqi.notification.mapper.NotificationMapper;
import com.shuaiqi.notification.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取通知列表
     */
    public NotificationListResponse getNotificationList(Long userId, Integer page, Integer pageSize) {
        Page<Notification> notificationPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);

        Page<Notification> result = notificationMapper.selectPage(notificationPage, wrapper);

        // 获取未读数量
        LambdaQueryWrapper<Notification> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        Long unreadCount = notificationMapper.selectCount(unreadWrapper);

        List<NotificationResponse> notificationList = result.getRecords().stream()
                .map(this::convertToResponse)
                .toList();

        return NotificationListResponse.builder()
                .list(notificationList)
                .total(result.getTotal())
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw BusinessException.notFound("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权操作此通知");
        }

        notification.setIsRead(true);
        notificationMapper.updateById(notification);
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true);
        notificationMapper.update(null, wrapper);
    }

    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw BusinessException.notFound("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此通知");
        }

        notificationMapper.deleteById(notificationId);
    }

    /**
     * 获取未读通知数量
     */
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        return notificationMapper.selectCount(wrapper);
    }

    /**
     * 创建通知
     */
    @Transactional
    public void createNotification(String type, String title, String content,
                                   Long userId, Long targetId, String targetType) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUserId(userId);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);

        // 通过 WebSocket 实时推送通知
        try {
            NotificationResponse response = convertToResponse(notification);
            String json = objectMapper.writeValueAsString(response);
            WebSocketServer.sendMessage(userId.toString(), json);
        } catch (Exception e) {
            log.error("WebSocket 推送通知失败: {}", e.getMessage());
        }
    }

    /**
     * 转换为响应对象
     */
    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .userId(notification.getUserId())
                .targetId(notification.getTargetId())
                .targetType(notification.getTargetType())
                .isRead(notification.getIsRead())
                .createTime(notification.getCreateTime())
                .build();
    }
}
