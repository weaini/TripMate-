package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.GuideRequest;
import com.huashidai.lvyouapp.dto.GuideResponse;
import com.huashidai.lvyouapp.dto.GuideCommentRequest;
import com.huashidai.lvyouapp.dto.GuideCommentResponse;
import com.huashidai.lvyouapp.service.GuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 攻略控制器
 */
@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
@Slf4j
public class GuideController {
    
    private final GuideService guideService;
    
    /**
     * 创建攻略
     */
    @PostMapping
    public ResponseEntity<GuideResponse> createGuide(@RequestBody GuideRequest request) {
        GuideResponse response = guideService.createGuide(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新攻略
     */
    @PutMapping("/{id}")
    public ResponseEntity<GuideResponse> updateGuide(@PathVariable Long id, @RequestBody GuideRequest request) {
        GuideResponse response = guideService.updateGuide(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除攻略
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuide(@PathVariable Long id) {
        guideService.deleteGuide(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取攻略详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<GuideResponse> getGuideById(@PathVariable Long id) {
        GuideResponse response = guideService.getGuideById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 分页获取攻略列表
     */
    @GetMapping
    public ResponseEntity<Page<GuideResponse>> getGuides(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<GuideResponse> response = guideService.getGuides(type, status, sortBy, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 搜索攻略
     */
    @GetMapping("/search")
    public ResponseEntity<Page<GuideResponse>> searchGuides(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<GuideResponse> response = guideService.searchGuides(keyword, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取我的攻略
     */
    @GetMapping("/my")
    public ResponseEntity<Page<GuideResponse>> getMyGuides(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<GuideResponse> response = guideService.getMyGuides(type, status, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 点赞/取消点赞攻略
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long id) {
        guideService.toggleLike(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 上传攻略封面图片
     */
    @PostMapping("/{id}/cover")
    public ResponseEntity<Void> uploadCoverImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        guideService.uploadCoverImage(id, file);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 创建评论
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<GuideCommentResponse> createComment(@PathVariable Long id, @RequestBody GuideCommentRequest request) {
        GuideCommentResponse response = guideService.createComment(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取攻略评论
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<GuideCommentResponse>> getGuideComments(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<GuideCommentResponse> response = guideService.getGuideComments(id, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> toggleCommentLike(@PathVariable Long commentId) {
        guideService.toggleCommentLike(commentId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        guideService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取攻略统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getGuideStats() {
        Object stats = guideService.getGuideStats();
        return ResponseEntity.ok(stats);
    }
}
