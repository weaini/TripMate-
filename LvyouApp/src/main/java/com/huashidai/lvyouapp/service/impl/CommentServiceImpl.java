package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.CommentRequest;
import com.huashidai.lvyouapp.dto.CommentResponse;
import com.huashidai.lvyouapp.dto.CommentListResponse;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.entity.Activity;
import com.huashidai.lvyouapp.entity.ActivityComment;
import com.huashidai.lvyouapp.entity.Comment;
import com.huashidai.lvyouapp.entity.Post;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.entity.Notification;
import com.huashidai.lvyouapp.repository.ActivityCommentRepository;
import com.huashidai.lvyouapp.repository.ActivityRepository;
import com.huashidai.lvyouapp.repository.CommentRepository;
import com.huashidai.lvyouapp.repository.PostRepository;
import com.huashidai.lvyouapp.exception.BusinessException;
import com.huashidai.lvyouapp.exception.ErrorCode;
import com.huashidai.lvyouapp.service.CommentService;
import com.huashidai.lvyouapp.service.PointsService;
import com.huashidai.lvyouapp.service.NotificationService;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    
    private final ActivityCommentRepository activityCommentRepository;
    private final ActivityRepository activityRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PointsService pointsService;
    private final NotificationService notificationService;
    
    @Override
    public CommentResponse createComment(Long activityId, CommentRequest request) {
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 查找活动
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));

        pointsService.assertEnoughPointsForComment(currentUser);
        
        // 创建评论
        ActivityComment comment = new ActivityComment();
        comment.setActivity(activity);
        comment.setUser(currentUser);
        comment.setContent(request.getContent());
        comment.setStatus(ActivityComment.CommentStatus.ACTIVE);
        
        // 如果有父评论，设置父评论
        if (request.getParentId() != null) {
            ActivityComment parent = activityCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            comment.setParent(parent);
        }
        
        ActivityComment savedComment = activityCommentRepository.save(comment);
        
        boolean pointsDeducted = pointsService.spendPointsForComment(currentUser, savedComment.getId());
        if (!pointsDeducted) {
            activityCommentRepository.delete(savedComment);
            throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT.getCode(),
                    "积分扣减未完成，可能因同时进行其他操作导致余额变化，评论未保存。请刷新后重试。", null);
        }
        
        log.info("用户 {} 在活动 {} 中发表评论，扣除1积分", currentUser.getUsername(), activityId);
        
        return convertToCommentResponse(savedComment);
    }
    
    @Override
    public Page<CommentResponse> getActivityComments(Long activityId, Pageable pageable) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        // 获取顶级评论
        Page<ActivityComment> comments = activityCommentRepository.findByActivityAndParentIsNullAndStatusOrderByCreatedAtDesc(
                activity, ActivityComment.CommentStatus.ACTIVE, pageable);
        
        return comments.map(this::convertToCommentResponse);
    }
    
    @Override
    public void deleteComment(Long commentId) {
        ActivityComment comment = activityCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 检查权限：只有评论作者可以删除
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权限删除此评论");
        }
        
        comment.setStatus(ActivityComment.CommentStatus.DELETED);
        activityCommentRepository.save(comment);
        
        log.info("用户 {} 删除了评论 {}", currentUser.getUsername(), commentId);
    }
    
    @Override
    public void likeComment(Long commentId) {
        // 简化实现，实际应该创建点赞记录
        // 先尝试查找活动评论
        try {
            ActivityComment activityComment = activityCommentRepository.findById(commentId)
                    .orElse(null);
            if (activityComment != null) {
                activityComment.setLikeCount(activityComment.getLikeCount() + 1);
                activityCommentRepository.save(activityComment);
                log.info("活动评论 {} 被点赞", commentId);
                return;
            }
        } catch (Exception e) {
            // 忽略活动评论查找失败
        }
        
        // 再尝试查找动态评论
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("评论不存在"));
            
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentRepository.save(comment);
            
            log.info("动态评论 {} 被点赞", commentId);
        } catch (Exception e) {
            throw new RuntimeException("评论不存在");
        }
    }
    
    @Override
    public void unlikeComment(Long commentId) {
        // 简化实现，实际应该删除点赞记录
        // 先尝试查找活动评论
        try {
            ActivityComment activityComment = activityCommentRepository.findById(commentId)
                    .orElse(null);
            if (activityComment != null) {
                if (activityComment.getLikeCount() > 0) {
                    activityComment.setLikeCount(activityComment.getLikeCount() - 1);
                    activityCommentRepository.save(activityComment);
                }
                log.info("活动评论 {} 取消点赞", commentId);
                return;
            }
        } catch (Exception e) {
            // 忽略活动评论查找失败
        }
        
        // 再尝试查找动态评论
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("评论不存在"));
            
            if (comment.getLikeCount() > 0) {
                comment.setLikeCount(comment.getLikeCount() - 1);
                commentRepository.save(comment);
            }
            
            log.info("动态评论 {} 取消点赞", commentId);
        } catch (Exception e) {
            throw new RuntimeException("评论不存在");
        }
    }
    
    private CommentResponse convertToCommentResponse(ActivityComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        response.setLikeCount(comment.getLikeCount());
        response.setStatus(comment.getStatus().name());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setIsLiked(false); // 简化处理
        
        // 设置用户信息
        if (comment.getUser() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(comment.getUser().getId());
            userResponse.setUsername(comment.getUser().getUsername());
            userResponse.setNickname(comment.getUser().getNickname());
            userResponse.setAvatar(comment.getUser().getAvatar());
            response.setUser(userResponse);
        }
        
        // 获取回复（如果是顶级评论）
        if (comment.getParent() == null) {
            List<ActivityComment> replies = activityCommentRepository.findByParentAndStatusOrderByCreatedAtAsc(
                    comment, ActivityComment.CommentStatus.ACTIVE);
            response.setReplies(replies.stream()
                    .map(this::convertToCommentResponse)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
    
    @Override
    public CommentResponse createPostComment(Long postId, CommentRequest request) {
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 查找动态
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));

        pointsService.assertEnoughPointsForComment(currentUser);
        
        // 创建评论
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(currentUser);
        comment.setContent(request.getContent());
        comment.setStatus(Comment.CommentStatus.ACTIVE);
        
        // 如果有父评论
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            comment.setParent(parent);
        }
        
        Comment savedComment = commentRepository.save(comment);
        
        boolean pointsDeducted = pointsService.spendPointsForComment(currentUser, savedComment.getId());
        if (!pointsDeducted) {
            commentRepository.delete(savedComment);
            throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT.getCode(),
                    "积分扣减未完成，可能因同时进行其他操作导致余额变化，评论未保存。请刷新后重试。", null);
        }
        
        // 更新帖子上的评论计数
        try {
            Integer currentCount = post.getCommentCount();
            if (currentCount == null) {
                currentCount = 0;
            }
            post.setCommentCount(currentCount + 1);
            postRepository.save(post);
        } catch (Exception e) {
            // 若计数更新失败，仅记录日志，不影响评论本身
            log.warn("更新动态 {} 的评论数失败: {}", postId, e.getMessage());
        }

        log.info("用户 {} 在动态 {} 中发表评论，扣除1积分，当前评论数: {}", 
                currentUser.getUsername(), postId, post.getCommentCount());

        // 向动态作者发送通知
        if (post.getUser() != null && !post.getUser().getId().equals(currentUser.getId())) {
            notificationService.createNotification(
                    post.getUser().getId(),
                    "有人评论了你的动态",
                    currentUser.getNickname() + ": " + request.getContent(),
                    Notification.NotificationType.COMMENT,
                    "/posts/" + postId
            );
        }
        
        return convertToPostCommentResponse(savedComment);
    }
    
    @Override
    public Page<CommentResponse> getPostComments(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 获取顶级评论
        Page<Comment> comments = commentRepository.findTopLevelCommentsByPost(
                post, Comment.CommentStatus.ACTIVE, pageable);
        
        return comments.map(this::convertToPostCommentResponse);
    }
    
    @Override
    public Page<CommentResponse> getCommentReplies(Long commentId, Pageable pageable) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        // 获取回复
        List<Comment> replies = commentRepository.findByParentAndStatusOrderByCreatedAtAsc(
                parentComment, Comment.CommentStatus.ACTIVE);
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), replies.size());
        List<Comment> pagedReplies = replies.subList(start, end);
        
        // 创建分页响应
        Page<Comment> page = new org.springframework.data.domain.PageImpl<>(
                pagedReplies, pageable, replies.size());
        
        return page.map(this::convertToPostCommentResponse);
    }
    
    private CommentResponse convertToPostCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        response.setLikeCount(comment.getLikeCount());
        response.setStatus(comment.getStatus().name());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setIsLiked(false); // 简化处理
        
        // 设置用户信息
        if (comment.getUser() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(comment.getUser().getId());
            userResponse.setUsername(comment.getUser().getUsername());
            userResponse.setNickname(comment.getUser().getNickname());
            userResponse.setAvatar(comment.getUser().getAvatar());
            response.setUser(userResponse);
        }
        
        return response;
    }
    
    @Override
    public CommentListResponse getPostCommentsWithStats(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 获取顶级评论
        Page<Comment> topLevelComments = commentRepository.findTopLevelCommentsByPost(
                post, Comment.CommentStatus.ACTIVE, pageable);
        
        // 统计信息
        Long totalComments = commentRepository.countByPostAndStatus(post, Comment.CommentStatus.ACTIVE);
        Long topLevelCount = commentRepository.countTopLevelCommentsByPost(post, Comment.CommentStatus.ACTIVE);
        Long secondLevelCount = totalComments - topLevelCount;
        
        // 为每个顶级评论加载二级评论
        List<CommentResponse> commentResponses = topLevelComments.getContent().stream()
                .map(comment -> {
                    CommentResponse response = convertToPostCommentResponse(comment);
                    
                    // 加载二级评论
                    List<Comment> replies = commentRepository.findByParentAndStatusOrderByCreatedAtAsc(
                            comment, Comment.CommentStatus.ACTIVE);
                    response.setReplies(replies.stream()
                            .map(this::convertToPostCommentResponse)
                            .collect(Collectors.toList()));
                    
                    return response;
                })
                .collect(Collectors.toList());
        
        // 构建响应
        CommentListResponse response = new CommentListResponse();
        response.setComments(commentResponses);
        response.setTotalComments(totalComments);
        response.setTopLevelComments(topLevelCount);
        response.setSecondLevelComments(secondLevelCount);
        response.setCurrentPage(pageable.getPageNumber());
        response.setPageSize(pageable.getPageSize());
        response.setTotalPages(topLevelComments.getTotalPages());
        response.setHasMore(pageable.getPageNumber() < topLevelComments.getTotalPages() - 1);
        
        return response;
    }
}