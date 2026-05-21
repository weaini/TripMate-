package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.PasswordChangeRequest;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser();
    
    /**
     * 更新用户资料
     */
    UserResponse updateProfile(UserUpdateRequest request);
    
    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file);
    
    /**
     * 修改密码
     */
    void changePassword(PasswordChangeRequest request);
    
    /**
     * 获取用户动态
     */
    Page<Map<String, Object>> getUserPosts(Long userId, Pageable pageable);
    
    /**
     * 获取用户关注列表
     */
    Page<UserResponse> getUserFollowing(Long userId, Pageable pageable);
    
    /**
     * 获取用户粉丝列表
     */
    Page<UserResponse> getUserFollowers(Long userId, Pageable pageable);
    
    /**
     * 关注用户
     */
    void followUser(Long userId);
    
    /**
     * 取消关注用户
     */
    void unfollowUser(Long userId);

    /**
     * 当前登录用户是否已关注 targetUserId
     */
    boolean isFollowing(Long targetUserId);
}
