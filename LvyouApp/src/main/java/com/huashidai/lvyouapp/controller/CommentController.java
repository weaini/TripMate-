package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.CommentRequest;
import com.huashidai.lvyouapp.dto.CommentResponse;
import com.huashidai.lvyouapp.dto.CommentListResponse;
import com.huashidai.lvyouapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 创建评论
     */
    @PostMapping("/activities/{activityId}")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long activityId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.createComment(activityId, request);
        
        // 通过WebSocket发送实时消息
        messagingTemplate.convertAndSend("/topic/activity/" + activityId + "/comments", response);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取活动评论列表
     */
    @GetMapping("/activities/{activityId}")
    public ResponseEntity<Page<CommentResponse>> getActivityComments(
            @PathVariable Long activityId,
            Pageable pageable) {
        Page<CommentResponse> response = commentService.getActivityComments(activityId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("删除成功");
    }
    
    /**
     * 点赞评论
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId) {
        commentService.likeComment(commentId);
        return ResponseEntity.ok("点赞成功");
    }
    
    /**
     * 取消点赞
     */
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<?> unlikeComment(@PathVariable Long commentId) {
        commentService.unlikeComment(commentId);
        return ResponseEntity.ok("取消点赞成功");
    }
    
    /**
     * 创建动态评论
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<CommentResponse> createPostComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.createPostComment(postId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取动态评论列表
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<Page<CommentResponse>> getPostComments(
            @PathVariable Long postId,
            Pageable pageable) {
        Page<CommentResponse> response = commentService.getPostComments(postId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取评论的回复
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentResponse>> getCommentReplies(
            @PathVariable Long commentId,
            Pageable pageable) {
        Page<CommentResponse> response = commentService.getCommentReplies(commentId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取动态评论列表（包含所有层级和统计信息）
     */
    @GetMapping("/posts/{postId}/with-stats")
    public ResponseEntity<CommentListResponse> getPostCommentsWithStats(
            @PathVariable Long postId,
            Pageable pageable) {
        CommentListResponse response = commentService.getPostCommentsWithStats(postId, pageable);
        return ResponseEntity.ok(response);
    }
}