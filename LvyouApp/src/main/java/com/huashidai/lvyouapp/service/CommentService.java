package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.CommentRequest;
import com.huashidai.lvyouapp.dto.CommentResponse;
import com.huashidai.lvyouapp.dto.CommentListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 创建评论
     */
    CommentResponse createComment(Long activityId, CommentRequest request);
    
    /**
     * 获取活动评论列表
     */
    Page<CommentResponse> getActivityComments(Long activityId, Pageable pageable);
    
    /**
     * 删除评论
     */
    void deleteComment(Long commentId);
    
    /**
     * 点赞评论
     */
    void likeComment(Long commentId);
    
    /**
     * 取消点赞
     */
    void unlikeComment(Long commentId);
    
    /**
     * 创建动态评论
     */
    CommentResponse createPostComment(Long postId, CommentRequest request);
    
    /**
     * 获取动态评论列表
     */
    Page<CommentResponse> getPostComments(Long postId, Pageable pageable);
    
    /**
     * 获取评论的回复
     */
    Page<CommentResponse> getCommentReplies(Long commentId, Pageable pageable);
    
    /**
     * 获取动态评论列表（包含所有层级和统计信息）
     */
    CommentListResponse getPostCommentsWithStats(Long postId, Pageable pageable);
}