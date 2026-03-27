package com.shuaiqi.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.user.dto.*;
import com.shuaiqi.user.entity.User;
import com.shuaiqi.user.entity.UserFollow;
import com.shuaiqi.user.mapper.UserFollowMapper;
import com.shuaiqi.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String UPLOAD_DIR = "uploads/avatars/";

    /**
     * 获取用户信息
     */
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return convertToResponse(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        return convertToResponse(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.badRequest("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 上传头像
     */
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            throw BusinessException.badRequest("只支持jpg、jpeg、png、gif格式的图片");
        }

        // 验证文件大小 (最大5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw BusinessException.badRequest("图片大小不能超过5MB");
        }

        try {
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            File destFile = new File(UPLOAD_DIR + newFilename);
            file.transferTo(destFile);

            // 更新用户头像
            String avatarUrl = "/api/uploads/avatars/" + newFilename;
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            return avatarUrl;
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw BusinessException.error("上传头像失败");
        }
    }

    /**
     * 获取用户详情（公开信息）
     */
    public UserInfoResponse getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        // 只返回公开信息
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 获取用户列表（管理员）
     */
    public Page<UserInfoResponse> getUserList(Integer page, Integer pageSize, String keyword) {
        Page<User> userPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword);
        }

        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(userPage, wrapper);

        Page<UserInfoResponse> responsePage = new Page<>();
        responsePage.setCurrent(result.getCurrent());
        responsePage.setSize(result.getSize());
        responsePage.setTotal(result.getTotal());

        List<UserInfoResponse> userList = result.getRecords().stream()
                .map(this::convertToResponse)
                .toList();
        responsePage.setRecords(userList);

        return responsePage;
    }

    /**
     * 删除用户（管理员）
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        // 软删除
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 关注用户
     */
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw BusinessException.badRequest("不能关注自己");
        }

        // 检查用户是否存在
        User followingUser = userMapper.selectById(followingId);
        if (followingUser == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        if (userFollowMapper.selectCount(wrapper) > 0) {
            throw BusinessException.badRequest("已经关注过了");
        }

        // 创建关注关系
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        userFollow.setCreateTime(LocalDateTime.now());
        userFollowMapper.insert(userFollow);
    }

    /**
     * 取消关注
     */
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);

        if (userFollowMapper.selectCount(wrapper) == 0) {
            throw BusinessException.badRequest("还没有关注");
        }

        userFollowMapper.delete(wrapper);
    }

    /**
     * 获取关注列表
     */
    public FollowListResponse getFollowingList(Long userId, Integer page, Integer pageSize) {
        Page<UserFollow> followPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        Page<UserFollow> result = userFollowMapper.selectPage(followPage, wrapper);

        List<FollowUserResponse> userList = result.getRecords().stream()
                .map(follow -> {
                    User user = userMapper.selectById(follow.getFollowingId());
                    return convertToFollowUserResponse(user, userId);
                })
                .toList();

        return FollowListResponse.builder()
                .list(userList)
                .total(result.getTotal())
                .build();
    }

    /**
     * 获取粉丝列表
     */
    public FollowListResponse getFollowerList(Long userId, Integer page, Integer pageSize) {
        Page<UserFollow> followPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowingId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        Page<UserFollow> result = userFollowMapper.selectPage(followPage, wrapper);

        List<FollowUserResponse> userList = result.getRecords().stream()
                .map(follow -> {
                    User user = userMapper.selectById(follow.getFollowerId());
                    return convertToFollowUserResponse(user, userId);
                })
                .toList();

        return FollowListResponse.builder()
                .list(userList)
                .total(result.getTotal())
                .build();
    }

    /**
     * 检查是否关注
     */
    public boolean checkIsFollowing(Long followerId, Long followingId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    /**
     * 转换为响应对象
     */
    private UserInfoResponse convertToResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    /**
     * 转换为关注用户响应对象
     */
    private FollowUserResponse convertToFollowUserResponse(User user, Long currentUserId) {
        boolean isFollowing = checkIsFollowing(currentUserId, user.getId());
        return FollowUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .isFollowing(isFollowing)
                .build();
    }
}
