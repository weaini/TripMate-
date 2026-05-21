package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.GuideRequest;
import com.huashidai.lvyouapp.dto.GuideResponse;
import com.huashidai.lvyouapp.dto.GuideCommentRequest;
import com.huashidai.lvyouapp.dto.GuideCommentResponse;
import com.huashidai.lvyouapp.entity.Guide;
import com.huashidai.lvyouapp.entity.GuideComment;
import com.huashidai.lvyouapp.entity.GuideLike;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.exception.BusinessException;
import com.huashidai.lvyouapp.exception.ErrorCode;
import com.huashidai.lvyouapp.repository.GuideRepository;
import com.huashidai.lvyouapp.repository.GuideCommentRepository;
import com.huashidai.lvyouapp.repository.GuideLikeRepository;
import com.huashidai.lvyouapp.service.GuideService;
import com.huashidai.lvyouapp.service.PointsService;
import com.huashidai.lvyouapp.util.FileStorageUtil;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 攻略服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GuideServiceImpl implements GuideService {
    
    private final GuideRepository guideRepository;
    private final GuideCommentRepository guideCommentRepository;
    private final GuideLikeRepository guideLikeRepository;
    private final PointsService pointsService;
    
    @Override
    public GuideResponse createGuide(GuideRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Guide guide = new Guide();
        guide.setTitle(request.getTitle());
        guide.setContent(request.getContent());
        guide.setSummary(request.getSummary());
        guide.setCoverImage(request.getCoverImage());
        guide.setType(Guide.GuideType.valueOf(request.getType()));
        guide.setStatus(Guide.GuideStatus.valueOf(request.getStatus()));
        guide.setAuthor(currentUser);
        
        Guide savedGuide = guideRepository.save(guide);
        
        // 发表攻略获得10积分
        pointsService.earnPointsForGuide(currentUser, savedGuide.getId());
        
        log.info("用户 {} 创建攻略成功，攻略ID: {}，获得10积分", currentUser.getUsername(), savedGuide.getId());
        
        return convertToGuideResponse(savedGuide);
    }
    
    @Override
    public GuideResponse updateGuide(Long id, GuideRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        // 检查权限：只有作者可以修改
        if (!guide.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权限修改此攻略");
        }
        
        guide.setTitle(request.getTitle());
        guide.setContent(request.getContent());
        guide.setSummary(request.getSummary());
        guide.setCoverImage(request.getCoverImage());
        guide.setType(Guide.GuideType.valueOf(request.getType()));
        guide.setStatus(Guide.GuideStatus.valueOf(request.getStatus()));
        
        Guide savedGuide = guideRepository.save(guide);
        
        log.info("攻略 {} 更新成功", id);
        
        return convertToGuideResponse(savedGuide);
    }
    
    @Override
    public void deleteGuide(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        // 检查权限：只有作者可以删除
        if (!guide.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权限删除此攻略");
        }
        
        guide.setStatus(Guide.GuideStatus.DELETED);
        guideRepository.save(guide);
        
        log.info("攻略 {} 已删除", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public GuideResponse getGuideById(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        // 增加浏览量
        guide.setViewCount(guide.getViewCount() + 1);
        guideRepository.save(guide);
        
        return convertToGuideResponse(guide);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GuideResponse> getGuides(String type, String status, String sortBy, Pageable pageable) {
        Guide.GuideStatus guideStatus = status != null ? Guide.GuideStatus.valueOf(status) : Guide.GuideStatus.PUBLISHED;
        Page<Guide> guides;
        
        if (type != null) {
            Guide.GuideType guideType = Guide.GuideType.valueOf(type);
            guides = guideRepository.findByTypeAndStatusOrderByCreatedAtDesc(guideType, guideStatus, pageable);
        } else {
            guides = guideRepository.findByStatusOrderByCreatedAtDesc(guideStatus, pageable);
        }
        
        return guides.map(this::convertToGuideResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GuideResponse> searchGuides(String keyword, Pageable pageable) {
        Page<Guide> guides = guideRepository.searchGuides(Guide.GuideStatus.PUBLISHED, keyword, pageable);
        return guides.map(this::convertToGuideResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GuideResponse> getMyGuides(String type, String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Page<Guide> guides;
        Guide.GuideStatus guideStatus = status != null ? Guide.GuideStatus.valueOf(status) : null;
        
        if (type != null && status != null) {
            Guide.GuideType guideType = Guide.GuideType.valueOf(type);
            guides = guideRepository.findByAuthorAndTypeAndStatusOrderByCreatedAtDesc(currentUser, guideType, guideStatus, pageable);
        } else if (type != null) {
            Guide.GuideType guideType = Guide.GuideType.valueOf(type);
            guides = guideRepository.findByAuthorAndTypeOrderByCreatedAtDesc(currentUser, guideType, pageable);
        } else if (status != null) {
            guides = guideRepository.findByAuthorAndStatusOrderByCreatedAtDesc(currentUser, guideStatus, pageable);
        } else {
            guides = guideRepository.findByAuthorOrderByCreatedAtDesc(currentUser, pageable);
        }
        
        return guides.map(this::convertToGuideResponse);
    }
    
    @Override
    public void incrementViewCount(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        guide.setViewCount(guide.getViewCount() + 1);
        guideRepository.save(guide);
    }
    
    @Override
    public void toggleLike(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        // 检查用户是否已经点赞
        boolean isLiked = guideLikeRepository.existsByGuideAndUser(guide, currentUser);
        
        if (isLiked) {
            // 取消点赞
            guideLikeRepository.deleteByGuideAndUser(guide, currentUser);
            guide.setLikeCount(Math.max(0, guide.getLikeCount() - 1));
            log.info("用户 {} 取消点赞攻略 {}", currentUser.getUsername(), id);
        } else {
            // 添加点赞
            GuideLike guideLike = new GuideLike();
            guideLike.setGuide(guide);
            guideLike.setUser(currentUser);
            guideLikeRepository.save(guideLike);
            guide.setLikeCount(guide.getLikeCount() + 1);
            log.info("用户 {} 点赞攻略 {}", currentUser.getUsername(), id);
        }
        
        guideRepository.save(guide);
    }
    
    @Override
    public void uploadCoverImage(Long id, MultipartFile file) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 检查权限
        if (!guide.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权限修改此攻略");
        }
        
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("没有上传文件");
        }
        
        try {
            String imageUrl = FileStorageUtil.saveFile(file, "guides");
            guide.setCoverImage(imageUrl);
            guideRepository.save(guide);

            log.info("攻略封面图片上传成功，攻略ID: {}, 文件路径: {}", id, imageUrl);
        } catch (Exception e) {
            log.error("上传攻略封面图片失败，攻略ID: {}", id, e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public GuideCommentResponse createComment(Long guideId, GuideCommentRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));

        pointsService.assertEnoughPointsForComment(currentUser);
        
        GuideComment comment = new GuideComment();
        comment.setContent(request.getContent());
        comment.setGuide(guide);
        comment.setUser(currentUser);
        
        // 如果有父评论
        if (request.getParentId() != null) {
            GuideComment parent = guideCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            comment.setParent(parent);
        }
        
        GuideComment savedComment = guideCommentRepository.save(comment);
        
        boolean pointsDeducted = pointsService.spendPointsForComment(currentUser, savedComment.getId());
        if (!pointsDeducted) {
            guideCommentRepository.delete(savedComment);
            throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT.getCode(),
                    "积分扣减未完成，可能因同时进行其他操作导致余额变化，评论未保存。请刷新后重试。", null);
        }
        
        // 更新攻略评论数
        guide.setCommentCount(guide.getCommentCount() + 1);
        guideRepository.save(guide);
        
        log.info("用户 {} 为攻略 {} 创建评论成功，扣除1积分", currentUser.getUsername(), guideId);
        
        return GuideCommentResponse.fromGuideComment(savedComment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GuideCommentResponse> getGuideComments(Long guideId, Pageable pageable) {
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        Page<GuideComment> comments = guideCommentRepository.findTopLevelCommentsByGuide(
                guide, GuideComment.CommentStatus.ACTIVE, pageable);
        
        return comments.map(GuideCommentResponse::fromGuideComment);
    }
    
    @Override
    public void toggleCommentLike(Long commentId) {
        // TODO: 实现评论点赞功能
        log.info("评论 {} 点赞功能待实现", commentId);
    }
    
    @Override
    public void deleteComment(Long commentId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        GuideComment comment = guideCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        // 检查权限：只有作者可以删除
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权限删除此评论");
        }
        
        comment.setStatus(GuideComment.CommentStatus.DELETED);
        guideCommentRepository.save(comment);
        
        log.info("评论 {} 已删除", commentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getGuideStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总攻略数
        Long totalGuides = guideRepository.countByStatus(Guide.GuideStatus.PUBLISHED);
        stats.put("totalGuides", totalGuides);
        
        // 各类型攻略数量
        List<Object[]> typeStats = guideRepository.countByTypeAndStatus(Guide.GuideStatus.PUBLISHED);
        Map<String, Long> typeCounts = new HashMap<>();
        for (Object[] stat : typeStats) {
            typeCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("typeStats", typeCounts);
        
        return stats;
    }
    
    /**
     * 转换Guide实体为GuideResponse
     */
    private GuideResponse convertToGuideResponse(Guide guide) {
        GuideResponse response = GuideResponse.fromGuide(guide);
        
        // 设置是否为作者和是否已点赞
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            response.setIsAuthor(guide.getAuthor().getId().equals(currentUser.getId()));
            // 检查当前用户是否已点赞
            boolean isLiked = guideLikeRepository.existsByGuideAndUser(guide, currentUser);
            response.setIsLiked(isLiked);
        }
        
        return response;
    }
}
