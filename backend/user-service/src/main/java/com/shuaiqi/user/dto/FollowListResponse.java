package com.shuaiqi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 关注列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowListResponse {

    /**
     * 用户列表
     */
    private List<FollowUserResponse> list;

    /**
     * 总数
     */
    private Long total;
}
