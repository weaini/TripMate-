package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.PointsRecordResponse;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.dto.UserUpdateRequest;
import com.huashidai.lvyouapp.dto.PasswordChangeRequest;
import com.huashidai.lvyouapp.entity.PointsRecord;
import com.huashidai.lvyouapp.service.PointsService;
import com.huashidai.lvyouapp.service.UserService;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final PointsService pointsService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取当前用户积分记录（分页）
     */
    @GetMapping("/me/points-records")
    public ResponseEntity<Page<PointsRecordResponse>> getMyPointsRecords(Pageable pageable) {
        if (SecurityUtils.getCurrentUser() == null) {
            return ResponseEntity.status(401).build();
        }
        log.info("获取积分记录: userId={}, page={}, size={}", SecurityUtils.getCurrentUser().getId(), pageable.getPageNumber(), pageable.getPageSize());
        Page<PointsRecord> page = pointsService.getUserPointsRecords(SecurityUtils.getCurrentUser(), pageable);
        List<PointsRecordResponse> content = page.getContent().stream()
                .map(PointsRecordResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                content, page.getPageable(), page.getTotalElements()));
    }
    
    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        try {
            String avatarUrl = userService.uploadAvatar(file);
            return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
        } catch (Exception e) {
            // 错误处理已在Service层完成，这里只是确保返回格式正确
            throw e;
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }
    
    /**
     * 获取用户动态
     */
    @GetMapping("/{userId}/posts")
    public ResponseEntity<Page<Map<String, Object>>> getUserPosts(@PathVariable Long userId, Pageable pageable) {
        Page<Map<String, Object>> response = userService.getUserPosts(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户关注列表
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<UserResponse>> getUserFollowing(@PathVariable Long userId, Pageable pageable) {
        Page<UserResponse> response = userService.getUserFollowing(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户粉丝列表
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<UserResponse>> getUserFollowers(@PathVariable Long userId, Pageable pageable) {
        Page<UserResponse> response = userService.getUserFollowers(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 关注用户
     */
    @PostMapping("/{userId}/follow")
    public ResponseEntity<?> followUser(@PathVariable Long userId) {
        userService.followUser(userId);
        return ResponseEntity.ok(Map.of("message", "关注成功"));
    }
    
    /**
     * 取消关注用户
     */
    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId) {
        userService.unfollowUser(userId);
        return ResponseEntity.ok(Map.of("message", "取消关注成功"));
    }

    /**
     * 当前用户是否已关注该用户（未登录返回 following: false）
     */
    @GetMapping("/{userId}/follow-status")
    public ResponseEntity<Map<String, Boolean>> followStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("following", userService.isFollowing(userId)));
    }
}
