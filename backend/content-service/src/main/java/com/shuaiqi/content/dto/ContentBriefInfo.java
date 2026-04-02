package com.shuaiqi.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentBriefInfo {
    private Long id;
    private Long authorId;
    private String title;
}
