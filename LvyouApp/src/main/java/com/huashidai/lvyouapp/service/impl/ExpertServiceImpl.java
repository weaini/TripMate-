package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.ExpertApplicationRequest;
import com.huashidai.lvyouapp.dto.ExpertApplicationResponse;
import com.huashidai.lvyouapp.entity.ExpertApplication;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.ExpertApplicationRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.service.ExpertService;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 达人服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpertServiceImpl implements ExpertService {
    
    private final ExpertApplicationRepository expertApplicationRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public void applyForExpert(ExpertApplicationRequest request) {
        // 获取当前登录用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 检查是否已经是达人
        if (currentUser.getRole() == User.UserRole.EXPERT) {
            throw new RuntimeException("您已经是旅游达人了");
        }
        
        // 检查是否已有待审核的申请
        ExpertApplication existingApplication = expertApplicationRepository.findByUserId(currentUser.getId()).orElse(null);
        if (existingApplication != null && existingApplication.getStatus() == ExpertApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("您已有待审核的达人申请");
        }
        
        // 创建新的申请
        ExpertApplication application = new ExpertApplication();
        application.setUser(currentUser);
        application.setReason(request.getReason());
        application.setBio(request.getBio());
        application.setExpertise(request.getExpertise());
        application.setContactInfo(request.getContactInfo());
        application.setStatus(ExpertApplication.ApplicationStatus.PENDING);
        application.setCreatedAt(LocalDateTime.now());
        
        expertApplicationRepository.save(application);
        
        log.info("用户申请成为旅游达人，用户ID: {}, 申请ID: {}", currentUser.getId(), application.getId());
    }
    
    @Override
    public ExpertApplicationResponse getMyApplication() {
        // 获取当前登录用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExpertApplication application = expertApplicationRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("您还没有申请成为旅游达人"));
        
        return convertToExpertApplicationResponse(application);
    }
    
    @Override
    @Transactional
    public void cancelApplication() {
        // 获取当前登录用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        ExpertApplication application = expertApplicationRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("您还没有申请成为旅游达人"));
        
        if (application.getStatus() != ExpertApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("只能取消待审核的申请");
        }
        
        expertApplicationRepository.delete(application);
        
        log.info("用户取消达人申请，用户ID: {}, 申请ID: {}", currentUser.getId(), application.getId());
    }
    
    private ExpertApplicationResponse convertToExpertApplicationResponse(ExpertApplication application) {
        ExpertApplicationResponse response = new ExpertApplicationResponse();
        response.setId(application.getId());
        response.setUserId(application.getUser().getId());
        response.setUsername(application.getUser().getUsername());
        response.setNickname(application.getUser().getNickname());
        response.setReason(application.getReason());
        response.setBio(application.getBio());
        response.setExpertise(application.getExpertise());
        response.setContactInfo(application.getContactInfo());
        response.setStatus(application.getStatus().name());
        response.setAdminNote(application.getAdminNote());
        response.setRejectReason(application.getRejectReason());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        response.setReviewedAt(application.getReviewedAt());
        
        return response;
    }
}
