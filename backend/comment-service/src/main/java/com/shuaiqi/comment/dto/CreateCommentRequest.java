package com.shuaiqi.comment.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建评论请求
 */
@Data
public class CreateCommentRequest {

    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
}
