package com.shuaiqi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuaiqi.common.dto.UserPublicInfo;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.dto.*;
import com.shuaiqi.entity.User;
import com.shuaiqi.entity.UserFollow;
import com.shuaiqi.mapper.UserFollowMapper;
import com.shuaiqi.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "user:token:";
    private static final String USER_TOKEN_INDEX_PREFIX = "user:token:index:";
    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars" + File.separator;

    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return convertToResponse(user);
    }

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
        user.setUpdateTime(java.time.LocalDateTime.now());
        userMapper.updateById(user);
        return convertToResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.badRequest("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(java.time.LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            throw BusinessException.badRequest("只支持jpg、jpeg、png、gif格式的图片");
        }
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw BusinessException.badRequest("非法的文件名");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw BusinessException.badRequest("图片大小不能超过5MB");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            File destFile = new File(UPLOAD_DIR + newFilename);
            file.transferTo(destFile);

            String avatarUrl = "/api/uploads/avatars/" + newFilename;
            user.setAvatar(avatarUrl);
            user.setUpdateTime(java.time.LocalDateTime.now());
            userMapper.updateById(user);
            return avatarUrl;
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw BusinessException.error("上传头像失败");
        }
    }

    public UserInfoResponse getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .createTime(user.getCreateTime())
                .build();
    }

    public UserPublicInfo getUserPublicInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return UserPublicInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .build();
    }

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
        responsePage.setRecords(result.getRecords().stream().map(this::convertToResponse).toList());
        return responsePage;
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        user.setStatus(0);
        user.setUpdateTime(java.time.LocalDateTime.now());
        userMapper.updateById(user);

        String userIdStr = userId.toString();
        Set<String> userTokens = redisTemplate.opsForSet().members(USER_TOKEN_INDEX_PREFIX + userIdStr);
        if (userTokens != null && !userTokens.isEmpty()) {
            for (String token : userTokens) {
                redisTemplate.delete(TOKEN_PREFIX + token);
            }
            redisTemplate.delete(USER_TOKEN_INDEX_PREFIX + userIdStr);
        }
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userIdStr);
        log.info("用户 {} 已注销，所有登录凭证已失效", userId);
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw BusinessException.badRequest("不能关注自己");
        }

        User followingUser = userMapper.selectById(followingId);
        if (followingUser == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (followingUser.getStatus() == null || followingUser.getStatus() != 1) {
            throw BusinessException.badRequest("该用户已注销或被禁用");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        if (userFollowMapper.selectCount(wrapper) > 0) {
            throw BusinessException.badRequest("已经关注过了");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        userFollow.setCreateTime(java.time.LocalDateTime.now());
        userFollowMapper.insert(userFollow);

        try {
            notificationService.createNotification("follow", "有人关注了你",
                    "用户关注了你", followingId, followerId, "user");
        } catch (Exception e) {
            log.warn("发送关注通知失败: followerId={}, followingId={}", followerId, followingId, e);
        }
    }

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

    public FollowListResponse getFollowingList(Long userId, Integer page, Integer pageSize) {
        Page<UserFollow> followPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        Page<UserFollow> result = userFollowMapper.selectPage(followPage, wrapper);

        List<Long> userIds = result.getRecords().stream()
                .map(UserFollow::getFollowingId)
                .toList();

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        Set<Long> followingIds = getFollowingIds(userId, userIds);

        final Map<Long, User> finalUserMap = userMap;
        List<FollowUserResponse> userList = result.getRecords().stream()
                .map(follow -> {
                    User user = finalUserMap.get(follow.getFollowingId());
                    return convertToFollowUserResponse(user, userId, followingIds);
                })
                .toList();

        return FollowListResponse.builder()
                .list(userList)
                .total(result.getTotal())
                .build();
    }

    public FollowListResponse getFollowerList(Long userId, Integer page, Integer pageSize) {
        Page<UserFollow> followPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowingId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        Page<UserFollow> result = userFollowMapper.selectPage(followPage, wrapper);

        List<Long> userIds = result.getRecords().stream()
                .map(UserFollow::getFollowerId)
                .toList();

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        Set<Long> followingIds = getFollowingIds(userId, userIds);

        final Map<Long, User> finalUserMap = userMap;
        List<FollowUserResponse> userList = result.getRecords().stream()
                .map(follow -> {
                    User user = finalUserMap.get(follow.getFollowerId());
                    return convertToFollowUserResponse(user, userId, followingIds);
                })
                .toList();

        return FollowListResponse.builder()
                .list(userList)
                .total(result.getTotal())
                .build();
    }

    private Set<Long> getFollowingIds(Long currentUserId, List<Long> targetUserIds) {
        if (targetUserIds.isEmpty()) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, currentUserId)
                .in(UserFollow::getFollowingId, targetUserIds);
        List<UserFollow> follows = userFollowMapper.selectList(wrapper);
        return follows.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toSet());
    }

    public boolean checkIsFollowing(Long followerId, Long followingId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    public Long getUserCount() {
        return userMapper.selectCount(null);
    }

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

    private FollowUserResponse convertToFollowUserResponse(User user, Long currentUserId, Set<Long> followingIds) {
        if (user == null) return null;
        boolean isFollowing = followingIds.contains(user.getId());
        return FollowUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .isFollowing(isFollowing)
                .build();
    }
}
