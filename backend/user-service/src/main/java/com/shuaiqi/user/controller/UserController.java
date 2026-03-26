package com.shuaiqi.user.controller;

import com.shuaiqi.common.result.Result;
import com.shuaiqi.user.dto.ChangePasswordRequest;
import com.shuaiqi.user.dto.UpdateUserRequest;
import com.shuaiqi.user.dto.UserInfoResponse;
import com.shuaiqi.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<UserInfoResponse> updateUserInfo(
            @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        UserInfoResponse userInfo = userService.updateUserInfo(userId, request);
        return Result.success("更新成功", userInfo);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @Validated @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success("上传成功", avatarUrl);
    }

    /**
     * 获取用户详情（公开信息）
     */
    @GetMapping("/{userId}")
    public Result<UserInfoResponse> getUserDetail(@PathVariable Long userId) {
        UserInfoResponse userInfo = userService.getUserDetail(userId);
        return Result.success(userInfo);
    }

    /**
     * 从请求中获取用户ID（实际项目中应该通过拦截器或过滤器获取）
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 这里简化处理，实际应该从JWT token中解析
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            throw new RuntimeException("未授权访问");
        }
        return Long.parseLong(userId);
    }
}
