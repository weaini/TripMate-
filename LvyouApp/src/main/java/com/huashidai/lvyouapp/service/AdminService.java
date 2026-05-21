package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 获取待审核的动态列表
     */
    Page<PostResponse> getPendingPosts(Pageable pageable);
    
    /**
     * 按状态获取动态列表（管理员，如 APPROVED、REJECTED）
     */
    Page<PostResponse> getPostsByStatus(String status, Pageable pageable);
    
    /**
     * 审核动态
     */
    void approvePost(Long postId, ApprovalRequest request);
    
    /**
     * 拒绝动态
     */
    void rejectPost(Long postId, RejectionRequest request);
    
    /**
     * 获取待审核的活动列表
     */
    Page<ActivityResponse> getPendingActivities(Pageable pageable);
    
    /**
     * 审核活动
     */
    void approveActivity(Long activityId, ApprovalRequest request);
    
    /**
     * 拒绝活动
     */
    void rejectActivity(Long activityId, RejectionRequest request);
    
    /**
     * 获取达人申请列表
     */
    Page<ExpertApplicationResponse> getExpertApplications(Pageable pageable);
    
    /**
     * 审核达人申请
     */
    void approveExpertApplication(Long applicationId, ApprovalRequest request);
    
    /**
     * 拒绝达人申请
     */
    void rejectExpertApplication(Long applicationId, RejectionRequest request);
    
    /**
     * 获取统计数据
     */
    AdminStatisticsResponse getStatistics();
    
    /**
     * 获取用户列表
     */
    Page<UserResponse> getUsers(String role, String keyword, Pageable pageable);
    
    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, String status);
    
    /**
     * 更新用户角色
     */
    void updateUserRole(Long userId, String role);
}
