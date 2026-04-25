package com.shuaiqi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.result.Result;
import com.shuaiqi.common.utils.RequestUtils;
import com.shuaiqi.dto.*;
import com.shuaiqi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @PutMapping("/info")
    public Result<UserInfoResponse> updateUserInfo(
            @Validated @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        Long userId = RequestUtils.getUserIdFromRequest(httpRequest);
        UserInfoResponse userInfo = userService.updateUserInfo(userId, request);
        return Result.success("更新成功", userInfo);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @Validated @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = RequestUtils.getUserIdFromRequest(httpRequest);
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            HttpServletRequest request) {
        Long userId = RequestUtils.getUserIdFromRequest(request);
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success("上传成功", avatarUrl);
    }

    @GetMapping("/{userId}")
    public Result<UserInfoResponse> getUserDetail(@PathVariable Long userId) {
        UserInfoResponse userInfo = userService.getUserDetail(userId);
        return Result.success(userInfo);
    }

    @GetMapping("/list")
    public Result<Page<UserInfoResponse>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<UserInfoResponse> userList = userService.getUserList(page, pageSize, keyword);
        return Result.success(userList);
    }

    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{userId}/follow")
    public Result<Void> followUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long followerId = RequestUtils.getUserIdFromRequest(request);
        userService.followUser(followerId, userId);
        return Result.success("关注成功", null);
    }

    @DeleteMapping("/{userId}/follow")
    public Result<Void> unfollowUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long followerId = RequestUtils.getUserIdFromRequest(request);
        userService.unfollowUser(followerId, userId);
        return Result.success("取消关注成功", null);
    }

    @GetMapping("/{userId}/following")
    public Result<FollowListResponse> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        FollowListResponse followingList = userService.getFollowingList(userId, page, pageSize);
        return Result.success(followingList);
    }

    @GetMapping("/{userId}/followers")
    public Result<FollowListResponse> getFollowerList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        FollowListResponse followerList = userService.getFollowerList(userId, page, pageSize);
        return Result.success(followerList);
    }

    @GetMapping("/{userId}/is-following")
    public Result<Boolean> checkIsFollowing(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long followerId = RequestUtils.getUserIdFromRequest(request);
        boolean isFollowing = userService.checkIsFollowing(followerId, userId);
        return Result.success(isFollowing);
    }

    @GetMapping("/count")
    public Result<Long> getUserCount() {
        Long count = userService.getUserCount();
        return Result.success(count);
    }
}
