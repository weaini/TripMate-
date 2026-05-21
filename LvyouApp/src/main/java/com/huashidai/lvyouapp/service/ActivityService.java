package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.ActivityCreateRequest;
import com.huashidai.lvyouapp.dto.ActivityResponse;
import com.huashidai.lvyouapp.dto.ActivityUpdateRequest;
import com.huashidai.lvyouapp.dto.ParticipantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {
    
    /**
     * 创建活动
     */
    ActivityResponse createActivity(ActivityCreateRequest request);
    
    /**
     * 更新活动
     */
    ActivityResponse updateActivity(Long id, ActivityUpdateRequest request);
    
    /**
     * 上传活动图片
     */
    void uploadImages(Long activityId, List<MultipartFile> files);
    
    /**
     * 获取活动列表
     */
    Page<ActivityResponse> getActivities(Pageable pageable);
    
    /**
     * 获取活动列表（带筛选条件）
     */
    Page<ActivityResponse> getActivities(String keyword, String type, String status, String sort, 
                                       String startDate, Double minCost, Double maxCost, Pageable pageable);
    
    /**
     * 获取活动详情
     */
    ActivityResponse getActivity(Long id);
    
    /**
     * 报名活动
     */
    void joinActivity(Long id, String note);
    
    /**
     * 取消报名
     */
    void cancelJoinActivity(Long id);
    
    /**
     * 审核报名
     */
    void approveParticipant(Long activityId, Long participantId);
    
    /**
     * 拒绝报名
     */
    void rejectParticipant(Long activityId, Long participantId, String reason);
    
    /**
     * 获取活动的参与者列表
     */
    Page<ParticipantResponse> getActivityParticipants(Long activityId, String status, Pageable pageable);
    
    /**
     * 获取我的报名记录
     */
    Page<ParticipantResponse> getMyParticipations(String status, Pageable pageable);
    
    /**
     * 删除活动
     */
    void deleteActivity(Long id);
    
    /**
     * 获取我的活动
     */
    Page<ActivityResponse> getMyActivities(String status, Pageable pageable);
    
    /**
     * 检查用户是否参与活动
     */
    java.util.Map<String, Object> checkUserParticipation(Long activityId);
}
