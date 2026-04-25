package com.shuaiqi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String title;

    private String content;

    private Long userId;

    private Long targetId;

    private String targetType;

    private Boolean isRead;

    private LocalDateTime createTime;
}
