package com.shuaiqi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.dto.NotificationListResponse;
import com.shuaiqi.dto.NotificationResponse;
import com.shuaiqi.entity.Notification;
import com.shuaiqi.mapper.NotificationMapper;
import com.shuaiqi.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;

    public NotificationListResponse getNotificationList(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100;

        Page<Notification> notificationPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);

        Page<Notification> result = notificationMapper.selectPage(notificationPage, wrapper);

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

    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true);
        notificationMapper.update(null, wrapper);
    }

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

    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        return notificationMapper.selectCount(wrapper);
    }

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

        try {
            NotificationResponse response = convertToResponse(notification);
            String json = objectMapper.writeValueAsString(response);
            WebSocketServer.sendMessage(userId.toString(), json);
        } catch (Exception e) {
            log.error("WebSocket 推送通知失败: {}", e.getMessage());
        }
    }

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
