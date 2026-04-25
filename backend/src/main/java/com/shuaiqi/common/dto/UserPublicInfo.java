package com.shuaiqi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPublicInfo {
    private Long id;
    private String username;
    private String avatar;
    private String bio;
}
