package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.GuideRequest;
import com.huashidai.lvyouapp.dto.GuideResponse;
import com.huashidai.lvyouapp.dto.GuideCommentRequest;
import com.huashidai.lvyouapp.dto.GuideCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 攻略服务接口
 */
public interface GuideService {
    
    /**
     * 创建攻略
     */
    GuideResponse createGuide(GuideRequest request);
    
    /**
     * 更新攻略
     */
    GuideResponse updateGuide(Long id, GuideRequest request);
    
    /**
     * 删除攻略
     */
    void deleteGuide(Long id);
    
    /**
     * 根据ID获取攻略详情
     */
    GuideResponse getGuideById(Long id);
    
    /**
     * 分页获取攻略列表
     */
    Page<GuideResponse> getGuides(String type, String status, String sortBy, Pageable pageable);
    
    /**
     * 搜索攻略
     */
    Page<GuideResponse> searchGuides(String keyword, Pageable pageable);
    
    /**
     * 获取我的攻略
     */
    Page<GuideResponse> getMyGuides(String type, String status, Pageable pageable);
    
    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);
    
    /**
     * 点赞/取消点赞攻略
     */
    void toggleLike(Long id);
    
    /**
     * 上传攻略封面图片
     */
    void uploadCoverImage(Long id, MultipartFile file);
    
    /**
     * 创建评论
     */
    GuideCommentResponse createComment(Long guideId, GuideCommentRequest request);
    
    /**
     * 获取攻略评论
     */
    Page<GuideCommentResponse> getGuideComments(Long guideId, Pageable pageable);
    
    /**
     * 点赞/取消点赞评论
     */
    void toggleCommentLike(Long commentId);
    
    /**
     * 删除评论
     */
    void deleteComment(Long commentId);
    
    /**
     * 获取攻略统计信息
     */
    Object getGuideStats();
}
