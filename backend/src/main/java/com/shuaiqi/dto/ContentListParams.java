package com.shuaiqi.dto;

import lombok.Data;

@Data
public class ContentListParams {
    private Integer page = 1;
    private Integer pageSize = 20;
    private Long categoryId;
    private Long authorId;
    private String keyword;
    private String sortBy = "latest";
}
