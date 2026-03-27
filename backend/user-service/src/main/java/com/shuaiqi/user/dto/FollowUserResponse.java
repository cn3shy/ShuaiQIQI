package com.shuaiqi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关注用户响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 是否已关注
     */
    private Boolean isFollowing;
}
