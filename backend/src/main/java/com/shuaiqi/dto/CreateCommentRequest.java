package com.shuaiqi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
}
