package com.shuaiqi.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shuaiqi.auth.dto.AuthResponse;
import com.shuaiqi.auth.dto.LoginRequest;
import com.shuaiqi.auth.dto.RegisterRequest;
import com.shuaiqi.auth.entity.User;
import com.shuaiqi.auth.mapper.UserMapper;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String TOKEN_PREFIX = "user:token:";
    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 验证密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw BusinessException.badRequest("两次密码输入不一致");
        }

        // 检查用户名是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())) > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())) > 0) {
            throw BusinessException.badRequest("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 生成Token
        return generateAuthResponse(user);
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        // 生成Token
        return generateAuthResponse(user);
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        // 从Redis删除Token
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    /**
     * 刷新Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        // 验证刷新Token
        if (!JwtUtils.validateToken(refreshToken)) {
            throw BusinessException.unauthorized("刷新Token无效或已过期");
        }

        // 获取用户ID
        String userId = JwtUtils.getUserId(refreshToken);

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.unauthorized("用户不存在");
        }

        // 检查Redis中的刷新Token
        String storedRefreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw BusinessException.unauthorized("刷新Token无效");
        }

        // 生成新的Token
        return generateAuthResponse(user);
    }

    /**
     * 生成认证响应
     */
    private AuthResponse generateAuthResponse(User user) {
        // 生成访问Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        String accessToken = JwtUtils.generateAccessToken(user.getId().toString(), claims);

        // 生成刷新Token
        String refreshToken = UUID.randomUUID().toString();

        // 存储Token到Redis
        redisTemplate.opsForValue().set(TOKEN_PREFIX + accessToken, user.getId().toString(), 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId().toString(), refreshToken, 7, TimeUnit.DAYS);

        // 构建响应
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .avatar(user.getAvatar())
                        .bio(user.getBio())
                        .build())
                .build();
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        if (!JwtUtils.validateToken(token)) {
            return false;
        }
        // 检查Redis中是否存在
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));
    }

    /**
     * 获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            throw BusinessException.unauthorized("Token无效或已过期");
        }
        return Long.parseLong(JwtUtils.getUserId(token));
    }
}
