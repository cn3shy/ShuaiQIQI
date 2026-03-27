package com.shuaiqi.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通知列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {

    /**
     * 通知列表
     */
    private List<NotificationResponse> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 未读数量
     */
    private Long unreadCount;
}
