package com.shuaiqi.content.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建内容请求
 */
@Data
public class CreateContentRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    private String content;

    private String coverImage;

    private Long categoryId;
}
