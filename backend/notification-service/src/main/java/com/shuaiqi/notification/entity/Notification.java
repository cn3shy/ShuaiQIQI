package com.shuaiqi.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
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
     * 目标ID（内容ID或评论ID）
     */
    private Long targetId;

    /**
     * 目标类型: content, comment
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
