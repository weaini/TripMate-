package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.PostCreateRequest;
import com.huashidai.lvyouapp.dto.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 动态服务接口
 */
public interface PostService {
    
    /**
     * 创建动态
     */
    PostResponse createPost(PostCreateRequest request);

    /**
     * 编辑动态
     */
    PostResponse updatePost(Long id, PostCreateRequest request);
    
    /**
     * 上传动态图片
     */
    void uploadImages(Long postId, List<MultipartFile> files);
    
    /**
     * 获取动态列表
     */
    Page<PostResponse> getPosts(Pageable pageable);
    
    /**
     * 获取动态列表（带筛选条件）
     */
    Page<PostResponse> getPosts(String type, String sort, String startDate, Boolean following, String keyword, Pageable pageable);
    
    /**
     * 获取用户动态
     */
    Page<PostResponse> getUserPosts(Long userId, Pageable pageable);

    /**
     * 获取当前登录用户自己的动态（可查看审核状态）
     */
    Page<PostResponse> getMyPosts(String status, Pageable pageable);
    
    /**
     * 获取动态详情
     */
    PostResponse getPost(Long id);
    
    /**
     * 点赞动态
     */
    void likePost(Long id);
    
    /**
     * 取消点赞
     */
    void unlikePost(Long id);
    
    /**
     * 删除动态
     */
    void deletePost(Long id);
    
    /**
     * 获取动态图片列表
     */
    List<String> getPostImages(Long postId);
    
    /**
     * 删除动态的所有图片
     */
    void deletePostImages(Long postId);
    
    /**
     * 获取最新动态
     */
    Page<PostResponse> getLatestPosts(Pageable pageable);
    
    /**
     * 获取热门动态
     */
    Page<PostResponse> getHotPosts(Pageable pageable);
}
