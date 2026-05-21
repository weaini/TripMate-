package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.*;
import com.huashidai.lvyouapp.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * 获取待审核的动态列表
     */
    @GetMapping("/posts/pending")
    public ResponseEntity<Page<PostResponse>> getPendingPosts(Pageable pageable) {
        Page<PostResponse> posts = adminService.getPendingPosts(pageable);
        return ResponseEntity.ok(posts);
    }
    
    /**
     * 按状态获取动态列表（管理员，status=APPROVED 或 REJECTED）
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponse>> getPostsByStatus(
            @RequestParam String status,
            Pageable pageable) {
        Page<PostResponse> posts = adminService.getPostsByStatus(status, pageable);
        return ResponseEntity.ok(posts);
    }
    
    /**
     * 审核动态
     */
    @PostMapping("/posts/{postId}/approve")
    public ResponseEntity<?> approvePost(@PathVariable Long postId, @RequestBody ApprovalRequest request) {
        adminService.approvePost(postId, request);
        return ResponseEntity.ok("动态审核完成");
    }
    
    /**
     * 拒绝动态
     */
    @PostMapping("/posts/{postId}/reject")
    public ResponseEntity<?> rejectPost(@PathVariable Long postId, @RequestBody RejectionRequest request) {
        adminService.rejectPost(postId, request);
        return ResponseEntity.ok("动态已拒绝");
    }
    
    /**
     * 获取待审核的活动列表
     */
    @GetMapping("/activities/pending")
    public ResponseEntity<Page<ActivityResponse>> getPendingActivities(Pageable pageable) {
        Page<ActivityResponse> activities = adminService.getPendingActivities(pageable);
        return ResponseEntity.ok(activities);
    }
    
    /**
     * 审核活动
     */
    @PostMapping("/activities/{activityId}/approve")
    public ResponseEntity<?> approveActivity(@PathVariable Long activityId, @RequestBody ApprovalRequest request) {
        adminService.approveActivity(activityId, request);
        return ResponseEntity.ok("活动审核完成");
    }
    
    /**
     * 拒绝活动
     */
    @PostMapping("/activities/{activityId}/reject")
    public ResponseEntity<?> rejectActivity(@PathVariable Long activityId, @RequestBody RejectionRequest request) {
        adminService.rejectActivity(activityId, request);
        return ResponseEntity.ok("活动已拒绝");
    }
    
    /**
     * 获取达人申请列表
     */
    @GetMapping("/expert-applications")
    public ResponseEntity<Page<ExpertApplicationResponse>> getExpertApplications(Pageable pageable) {
        Page<ExpertApplicationResponse> applications = adminService.getExpertApplications(pageable);
        return ResponseEntity.ok(applications);
    }
    
    /**
     * 审核达人申请
     */
    @PostMapping("/expert-applications/{applicationId}/approve")
    public ResponseEntity<?> approveExpertApplication(@PathVariable Long applicationId, @RequestBody ApprovalRequest request) {
        adminService.approveExpertApplication(applicationId, request);
        return ResponseEntity.ok("达人申请审核完成");
    }
    
    /**
     * 拒绝达人申请
     */
    @PostMapping("/expert-applications/{applicationId}/reject")
    public ResponseEntity<?> rejectExpertApplication(@PathVariable Long applicationId, @RequestBody RejectionRequest request) {
        adminService.rejectExpertApplication(applicationId, request);
        return ResponseEntity.ok("达人申请已拒绝");
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsResponse> getStatistics() {
        AdminStatisticsResponse statistics = adminService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Pageable pageable) {
        Page<UserResponse> users = adminService.getUsers(role, keyword, pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        adminService.updateUserStatus(userId, request.get("status"));
        return ResponseEntity.ok("用户状态更新成功");
    }
    
    /**
     * 更新用户角色
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        adminService.updateUserRole(userId, request.get("role"));
        return ResponseEntity.ok("用户角色更新成功");
    }
}
