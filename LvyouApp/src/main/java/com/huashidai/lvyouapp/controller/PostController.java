package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.PostCreateRequest;
import com.huashidai.lvyouapp.dto.PostResponse;
import com.huashidai.lvyouapp.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 动态控制器
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    /**
     * 发布动态
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 编辑动态
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.updatePost(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取动态列表
     */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) Boolean following,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Pageable pageable) {
        
        // 创建分页对象
        Pageable customPageable = PageRequest.of(page, size);
        
        Page<PostResponse> response = postService.getPosts(type, sort, startDate, following, keyword, customPageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取最新动态
     */
    @GetMapping("/latest")
    public ResponseEntity<Page<PostResponse>> getLatestPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Pageable pageable) {
        
        Pageable customPageable = PageRequest.of(page, size);
        Page<PostResponse> response = postService.getLatestPosts(customPageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取热门动态
     */
    @GetMapping("/hot")
    public ResponseEntity<Page<PostResponse>> getHotPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Pageable pageable) {
        
        Pageable customPageable = PageRequest.of(page, size);
        Page<PostResponse> response = postService.getHotPosts(customPageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户动态
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(@PathVariable Long userId, Pageable pageable) {
        Page<PostResponse> response = postService.getUserPosts(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户自己的动态（包含审核状态）
     */
    @GetMapping("/my")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<PostResponse> response = postService.getMyPosts(status, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取动态详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        PostResponse response = postService.getPost(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 点赞动态
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        postService.likePost(id);
        return ResponseEntity.ok("点赞成功");
    }
    
    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikePost(@PathVariable Long id) {
        postService.unlikePost(id);
        return ResponseEntity.ok("取消点赞成功");
    }
    
    /**
     * 删除动态
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("删除成功");
    }
    
    /**
     * 上传动态图片
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<?> uploadImages(@PathVariable Long id, 
                                        @RequestParam("files") List<MultipartFile> files) {
        postService.uploadImages(id, files);
        return ResponseEntity.ok("图片上传成功");
    }
    
    /**
     * 删除动态图片
     */
    @DeleteMapping("/{id}/images")
    public ResponseEntity<?> deleteImages(@PathVariable Long id) {
        postService.deletePostImages(id);
        return ResponseEntity.ok("图片删除成功");
    }
    
    /**
     * 获取动态图片列表
     */
    @GetMapping("/{id}/images")
    public ResponseEntity<List<String>> getImages(@PathVariable Long id) {
        List<String> images = postService.getPostImages(id);
        return ResponseEntity.ok(images);
    }
}