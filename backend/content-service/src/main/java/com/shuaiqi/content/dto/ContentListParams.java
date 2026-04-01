package com.shuaiqi.content.dto;

import lombok.Data;

/**
 * 内容列表查询参数
 */
@Data
public class ContentListParams {
    private Integer page = 1;
    private Integer pageSize = 20;
    private Long categoryId;
    private Long authorId;
    private String keyword;
    private String sortBy = "latest"; // latest, popular, hot
}
