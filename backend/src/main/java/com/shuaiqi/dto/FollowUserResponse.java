package com.shuaiqi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUserResponse {
    private Long id;
    private String username;
    private String avatar;
    private String bio;
    private Boolean isFollowing;
}
