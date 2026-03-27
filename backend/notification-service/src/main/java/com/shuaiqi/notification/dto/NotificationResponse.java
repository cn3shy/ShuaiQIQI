package com.shuaiqi.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    /**
     * 通知类型: comment, like, favorite, follow
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 接收者用户ID
     */
    private Long userId;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
