package com.shuaiqi.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.common.result.Result;
import com.shuaiqi.common.utils.RequestUtils;
import com.shuaiqi.user.dto.ChangePasswordRequest;
import com.shuaiqi.user.dto.FollowListResponse;
import com.shuaiqi.user.dto.UpdateUserRequest;
import com.shuaiqi.user.dto.UserInfoResponse;
import com.shuaiqi.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        Long userId = RequestUtils.getUserIdFromRequest(request);
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
        Long userId = RequestUtils.getUserIdFromRequest(httpRequest);
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
        Long userId = RequestUtils.getUserIdFromRequest(httpRequest);
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
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
     * 获取用户列表（管理员）
     */
    @GetMapping("/list")
    public Result<Page<UserInfoResponse>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<UserInfoResponse> userList = userService.getUserList(page, pageSize, keyword);
        return Result.success(userList);
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("删除成功", null);
    }

    /**
     * 关注用户
     */
    @PostMapping("/{userId}/follow")
    public Result<Void> followUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long followerId = RequestUtils.getUserIdFromRequest(request);
        userService.followUser(followerId, userId);
        return Result.success("关注成功", null);
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{userId}/follow")
    public Result<Void> unfollowUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long followerId = RequestUtils.getUserIdFromRequest(request);
        userService.unfollowUser(followerId, userId);
        return Result.success("取消关注成功", null);
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/{userId}/following")
    public Result<FollowListResponse> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        FollowListResponse followingList = userService.getFollowingList(userId, page, pageSize);
        return Result.success(followingList);
    }

    /**
     * 获取粉丝列表
     */
    @GetMapping("/{userId}/followers")
    public Result<FollowListResponse> getFollowerList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        FollowListResponse followerList = userService.getFollowerList(userId, page, pageSize);
        return Result.success(followerList);
    }

    /**
     * 检查是否关注
     */
    @GetMapping("/{userId}/is-following")
    public Result<Boolean> checkIsFollowing(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long followerId = RequestUtils.getUserIdFromRequest(request);
        boolean isFollowing = userService.checkIsFollowing(followerId, userId);
        return Result.success(isFollowing);
    }

    /**
     * 获取用户总数（管理员）
     */
    @GetMapping("/count")
    public Result<Long> getUserCount() {
        Long count = userService.getUserCount();
        return Result.success(count);
    }
}
