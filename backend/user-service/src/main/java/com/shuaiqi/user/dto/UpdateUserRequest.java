package com.shuaiqi.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;
}
