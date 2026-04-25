package com.shuaiqi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;
}
