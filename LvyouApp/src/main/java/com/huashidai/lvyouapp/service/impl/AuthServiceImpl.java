package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.LoginRequest;
import com.huashidai.lvyouapp.dto.RegisterRequest;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.entity.Post;
import com.huashidai.lvyouapp.entity.Activity;
import com.huashidai.lvyouapp.entity.Guide;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.repository.PostRepository;
import com.huashidai.lvyouapp.repository.ActivityRepository;
import com.huashidai.lvyouapp.repository.GuideRepository;
import com.huashidai.lvyouapp.exception.BusinessException;
import com.huashidai.lvyouapp.service.AuthService;
import com.huashidai.lvyouapp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ActivityRepository activityRepository;
    private final GuideRepository guideRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("用户注册请求: username={}, email={}", request.getUsername(), request.getEmail());
        
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("用户名已存在: {}", request.getUsername());
                throw new BusinessException("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("邮箱已存在: {}", request.getEmail());
                throw new BusinessException("邮箱已存在");
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
            user.setPhone(request.getPhone());
            user.setRole(User.UserRole.TOURIST);
            user.setStatus(User.UserStatus.ACTIVE);
            user.setPoints(0);
            user.setFollowersCount(0);
            user.setFollowingCount(0);
            user.setIsVerified(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            log.info("用户注册成功: id={}, username={}", savedUser.getId(), savedUser.getUsername());
            
            return convertToUserResponse(savedUser);
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Object login(LoginRequest request) {
        log.info("用户登录请求: {}", request.getUsernameOrEmail());
        
        // 先尝试通过用户名查找
        User user = userRepository.findByUsername(request.getUsernameOrEmail()).orElse(null);
        if (user == null) {
            log.info("用户名未找到，尝试通过邮箱查找: {}", request.getUsernameOrEmail());
            // 如果用户名没找到，尝试通过邮箱查找
            user = userRepository.findByEmail(request.getUsernameOrEmail()).orElse(null);
        }
        
        if (user == null) {
            log.warn("用户不存在: {}", request.getUsernameOrEmail());
            throw new BusinessException("用户名或邮箱不存在");
        }
        
        log.info("找到用户: {} (ID: {})", user.getUsername(), user.getId());
        
        // 验证密码
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("密码验证结果: {}", passwordMatches);
        
        if (!passwordMatches) {
            log.warn("密码错误，用户: {}", user.getUsername());
            throw new BusinessException("密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.warn("账户已被禁用，用户: {}, 状态: {}", user.getUsername(), user.getStatus());
            throw new BusinessException("账户已被禁用");
        }
        
        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("登录成功，用户: {}, Token已生成", user.getUsername());
        
        return new LoginResponse(token, convertToUserResponse(user));
    }
    
    @Override
    public void logout() {
        // 这里可以添加token黑名单逻辑
        log.info("用户登出");
    }
    
    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("请先登录");
        }
        
        // 检查principal是否是User对象
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return convertToUserResponse(user);
        }
        
        // 如果不是User对象，尝试通过用户名查找
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return convertToUserResponse(user);
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setGender(user.getGender());
        response.setAge(user.getAge());
        response.setLocation(user.getLocation());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setPoints(user.getPoints());
        response.setFollowersCount(user.getFollowersCount());
        response.setFollowingCount(user.getFollowingCount());
        response.setIsVerified(user.getIsVerified());
        response.setCreatedAt(user.getCreatedAt());
        
        // 计算用户发布的内容数量
        try {
            // 计算动态数量
            long postsCount = postRepository.countByUserIdAndStatus(user.getId(), Post.PostStatus.APPROVED);
            response.setPostsCount((int) postsCount);
            
            // 计算活动数量
            long activitiesCount = activityRepository.countByOrganizerIdAndStatus(user.getId(), Activity.ActivityStatus.APPROVED);
            response.setActivitiesCount((int) activitiesCount);
            
            // 计算攻略数量
            long strategiesCount = guideRepository.countByAuthorAndStatus(user, Guide.GuideStatus.PUBLISHED);
            response.setStrategiesCount((int) strategiesCount);
            
            log.info("用户 {} 的内容统计: 动态={}, 活动={}, 攻略={}", 
                    user.getUsername(), postsCount, activitiesCount, strategiesCount);
        } catch (Exception e) {
            log.warn("计算用户内容数量失败: {}", e.getMessage());
            // 设置默认值
            response.setPostsCount(0);
            response.setActivitiesCount(0);
            response.setStrategiesCount(0);
        }
        
        return response;
    }
    
    // 登录响应类
    public static class LoginResponse {
        private String token;
        private UserResponse user;
        
        public LoginResponse(String token, UserResponse user) {
            this.token = token;
            this.user = user;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public UserResponse getUser() {
            return user;
        }
        
        public void setUser(UserResponse user) {
            this.user = user;
        }
    }
}
