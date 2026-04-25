package com.shuaiqi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shuaiqi.common.exception.BusinessException;
import com.shuaiqi.common.utils.JwtUtils;
import com.shuaiqi.dto.*;
import com.shuaiqi.entity.User;
import com.shuaiqi.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String TOKEN_PREFIX = "user:token:";
    private static final String USER_TOKEN_INDEX_PREFIX = "user:token:index:";
    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";
    private static final String RESET_TOKEN_PREFIX = "user:reset:";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw BusinessException.badRequest("两次密码输入不一致");
        }
        validatePasswordStrength(request.getPassword());

        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())) > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }

        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())) > 0) {
            throw BusinessException.badRequest("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        return generateAuthResponse(user);
    }

    public void logout(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!JwtUtils.validateToken(refreshToken)) {
            throw BusinessException.unauthorized("刷新Token无效或已过期");
        }

        String userId = JwtUtils.getUserId(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.unauthorized("用户不存在");
        }

        String storedRefreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw BusinessException.unauthorized("刷新Token无效");
        }

        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole() != null ? user.getRole() : "user");
        String accessToken = JwtUtils.generateAccessToken(user.getId().toString(), claims);

        String refreshToken = JwtUtils.generateRefreshToken(user.getId().toString());

        String tokenKey = TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(tokenKey, user.getId().toString(), 24, TimeUnit.HOURS);
        redisTemplate.opsForSet().add(USER_TOKEN_INDEX_PREFIX + user.getId().toString(), accessToken);
        redisTemplate.expire(USER_TOKEN_INDEX_PREFIX + user.getId().toString(), 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId().toString(), refreshToken, 7, TimeUnit.DAYS);

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
                        .role(user.getRole() != null ? user.getRole() : "user")
                        .createTime(user.getCreateTime())
                        .updateTime(user.getUpdateTime())
                        .build())
                .build();
    }

    public boolean validateToken(String token) {
        if (!JwtUtils.validateToken(token)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));
    }

    public Long getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            throw BusinessException.unauthorized("Token无效或已过期");
        }
        return Long.parseLong(JwtUtils.getUserId(token));
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));

        if (user == null) {
            return;
        }

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + resetToken, user.getId().toString(), 30, TimeUnit.MINUTES);
        log.info("用户 {} 的密码重置令牌已生成，有效期30分钟", user.getUsername());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw BusinessException.badRequest("两次密码输入不一致");
        }

        String userId = redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + request.getToken());
        if (userId == null) {
            throw BusinessException.badRequest("重置令牌无效或已过期");
        }
        redisTemplate.delete(RESET_TOKEN_PREFIX + request.getToken());

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.badRequest("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        Set<String> userTokens = redisTemplate.opsForSet().members(USER_TOKEN_INDEX_PREFIX + userId);
        if (userTokens != null && !userTokens.isEmpty()) {
            for (String token : userTokens) {
                redisTemplate.delete(TOKEN_PREFIX + token);
            }
            redisTemplate.delete(USER_TOKEN_INDEX_PREFIX + userId);
        }
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);

        log.info("用户 {} 密码已重置", user.getUsername());
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            throw BusinessException.badRequest("密码长度不能少于6位");
        }
        if (password.length() > 32) {
            throw BusinessException.badRequest("密码长度不能超过32位");
        }
        if (!password.matches(".*[A-Za-z].*")) {
            throw BusinessException.badRequest("密码必须包含至少一个字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw BusinessException.badRequest("密码必须包含至少一个数字");
        }
    }
}
