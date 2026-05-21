package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.PasswordChangeRequest;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.dto.UserUpdateRequest;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.entity.Guide;
import com.huashidai.lvyouapp.entity.UserFollow;
import com.huashidai.lvyouapp.exception.BusinessException;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.repository.GuideRepository;
import com.huashidai.lvyouapp.repository.UserFollowRepository;
import com.huashidai.lvyouapp.service.UserService;
import com.huashidai.lvyouapp.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final GuideRepository guideRepository;
    private final UserFollowRepository userFollowRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("AUTH_REQUIRED", "用户未登录，请先登录");
        }
        
        // 如果认证主体是User对象，直接使用
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return convertToUserResponse(user);
        }
        
        // 否则通过用户名查找
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在，请重新登录"));
        
        return convertToUserResponse(user);
    }
    
    @Override
    public UserResponse updateProfile(UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("AUTH_REQUIRED", "用户未登录，请先登录");
        }
        
        User user;
        // 如果认证主体是User对象，直接使用
        if (authentication.getPrincipal() instanceof User) {
            user = (User) authentication.getPrincipal();
            // 从数据库重新获取最新数据，避免使用过期的用户信息
            user = userRepository.findById(user.getId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在，请重新登录"));
        } else {
            // 否则通过用户名查找
            String username = authentication.getName();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在，请重新登录"));
        }
        
        // 更新用户信息
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim();
            if (!email.equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(email)) {
                throw new BusinessException("该邮箱已被其他账号使用");
            }
            user.setEmail(email);
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getInterests() != null) {
            user.setInterests(request.getInterests());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        
        log.info("用户 {} 更新了个人资料", user.getUsername());
        return convertToUserResponse(savedUser);
    }
    
    @Override
    public String uploadAvatar(MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "请选择要上传的文件");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("INVALID_FILE_TYPE", "只能上传图片文件，支持JPG、PNG格式");
        }
        
        // 验证文件大小 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("FILE_TOO_LARGE", "图片大小不能超过10MB，请选择较小的文件");
        }
        
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("AUTH_REQUIRED", "用户未登录，请先登录");
        }
        
        User currentUser;
        // 如果认证主体是User对象，直接使用
        if (authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
            // 从数据库重新获取最新数据
            currentUser = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在，请重新登录"));
        } else {
            // 否则通过用户名查找
            String username = authentication.getName();
            currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在，请重新登录"));
        }
        
        String oldAvatarUrl = currentUser.getAvatar();
        
        try {
            // 使用文件存储工具保存头像
            String avatarUrl = FileStorageUtil.saveFile(file, "avatars");
            log.info("头像上传成功: {}", avatarUrl);
            
            // 更新用户头像URL
            currentUser.setAvatar(avatarUrl);
            userRepository.save(currentUser);
            
            // 删除旧头像文件（如果存在且不是默认头像）
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty() && 
                !oldAvatarUrl.startsWith("http") && // 不是外部链接
                !oldAvatarUrl.equals(avatarUrl)) { // 不是同一个文件
                try {
                    boolean deleted = FileStorageUtil.deleteFile(oldAvatarUrl);
                    if (deleted) {
                        log.info("旧头像删除成功: {}", oldAvatarUrl);
                    } else {
                        log.warn("旧头像删除失败: {}", oldAvatarUrl);
                    }
                } catch (Exception e) {
                    log.warn("删除旧头像时出错: {}", e.getMessage());
                    // 不抛出异常，因为新头像已经上传成功
                }
            }
            
            return avatarUrl;
            
        } catch (Exception e) {
            log.error("头像上传失败: {}", e.getMessage());
            throw new BusinessException("UPLOAD_FAILED", "头像上传失败，请重试");
        }
    }
    
    @Override
    public void changePassword(PasswordChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("AUTH_REQUIRED", "用户未登录，请先登录");
        }
        
        User user;
        // 如果认证主体是User对象，直接使用
        if (authentication.getPrincipal() instanceof User) {
            user = (User) authentication.getPrincipal();
            // 从数据库重新获取最新数据
            user = userRepository.findById(user.getId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在，请重新登录"));
        } else {
            // 否则通过用户名查找
            String username = authentication.getName();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在，请重新登录"));
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_OLD_PASSWORD", "原密码错误，请重新输入");
        }
        
        // 验证新密码和确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("PASSWORD_MISMATCH", "新密码和确认密码不一致，请重新输入");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户 {} 修改了密码", user.getUsername());
    }
    
    @Override
    public Page<Map<String, Object>> getUserPosts(Long userId, Pageable pageable) {
        // 这里应该调用PostService获取用户动态
        // 暂时返回空页面
        return Page.empty(pageable);
    }
    
    @Override
    public Page<UserResponse> getUserFollowing(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        Page<UserFollow> page = userFollowRepository.findByFollower_IdOrderByCreatedAtDesc(userId, pageable);
        return page.map(uf -> convertToUserResponse(uf.getFollowing()));
    }
    
    @Override
    public Page<UserResponse> getUserFollowers(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        Page<UserFollow> page = userFollowRepository.findByFollowing_IdOrderByCreatedAtDesc(userId, pageable);
        return page.map(uf -> convertToUserResponse(uf.getFollower()));
    }
    
    @Override
    @Transactional
    public void followUser(Long userId) {
        User currentUser = requireCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (currentUser.getId().equals(userId)) {
            throw new BusinessException("不能关注自己");
        }
        if (userFollowRepository.existsByFollower_IdAndFollowing_Id(currentUser.getId(), userId)) {
            log.debug("已关注用户 {}，忽略重复关注", userId);
            return;
        }
        UserFollow row = new UserFollow();
        row.setFollower(currentUser);
        row.setFollowing(targetUser);
        userFollowRepository.save(row);
        targetUser.setFollowersCount(safeInc(targetUser.getFollowersCount()));
        currentUser.setFollowingCount(safeInc(currentUser.getFollowingCount()));
        userRepository.save(targetUser);
        userRepository.save(currentUser);
        log.info("用户 {} 关注 {}", currentUser.getUsername(), targetUser.getUsername());
    }
    
    @Override
    @Transactional
    public void unfollowUser(Long userId) {
        User currentUser = requireCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Optional<UserFollow> rowOpt = userFollowRepository
                .findByFollower_IdAndFollowing_Id(currentUser.getId(), userId);
        if (rowOpt.isEmpty()) {
            log.debug("未关注用户 {}，忽略重复取消关注", userId);
            return;
        }
        UserFollow row = rowOpt.get();
        userFollowRepository.delete(row);
        targetUser.setFollowersCount(safeDec(targetUser.getFollowersCount()));
        currentUser.setFollowingCount(safeDec(currentUser.getFollowingCount()));
        userRepository.save(targetUser);
        userRepository.save(currentUser);
        log.info("用户 {} 取消关注 {}", currentUser.getUsername(), targetUser.getUsername());
    }

    @Override
    public boolean isFollowing(Long targetUserId) {
        User current = com.huashidai.lvyouapp.util.SecurityUtils.getCurrentUser();
        if (current == null || targetUserId == null) {
            return false;
        }
        return userFollowRepository.existsByFollower_IdAndFollowing_Id(current.getId(), targetUserId);
    }

    private User requireCurrentUser() {
        User u = com.huashidai.lvyouapp.util.SecurityUtils.getCurrentUser();
        if (u == null) {
            throw new BusinessException("请先登录");
        }
        return userRepository.findById(u.getId()).orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private static int safeInc(Integer n) {
        return (n == null ? 0 : n) + 1;
    }

    private static int safeDec(Integer n) {
        int v = n == null ? 0 : n;
        return Math.max(0, v - 1);
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        response.setBio(user.getBio());
        response.setGender(user.getGender());
        response.setBirthday(user.getBirthday());
        response.setLocation(user.getLocation());
        response.setInterests(user.getInterests());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setPoints(user.getPoints());
        response.setFollowersCount(user.getFollowersCount());
        response.setFollowingCount(user.getFollowingCount());
        response.setIsVerified(user.getIsVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        // 计算用户的攻略数量
        try {
            Long strategiesCount = guideRepository.countByAuthorAndStatus(user, Guide.GuideStatus.PUBLISHED);
            response.setStrategiesCount(strategiesCount != null ? strategiesCount.intValue() : 0);
        } catch (Exception e) {
            log.warn("计算用户攻略数量失败: {}", e.getMessage());
            response.setStrategiesCount(0);
        }
        
        return response;
    }
}
