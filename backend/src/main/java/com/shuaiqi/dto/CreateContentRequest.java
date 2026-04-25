package com.shuaiqi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateContentRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private Long categoryId;
}
