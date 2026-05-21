package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.PostCreateRequest;
import com.huashidai.lvyouapp.dto.PostResponse;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.entity.Post;
import com.huashidai.lvyouapp.entity.PostImage;
import com.huashidai.lvyouapp.entity.PostLike;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.exception.BusinessException;
import com.huashidai.lvyouapp.repository.PostImageRepository;
import com.huashidai.lvyouapp.repository.PostLikeRepository;
import com.huashidai.lvyouapp.repository.PostRepository;
import com.huashidai.lvyouapp.repository.UserFollowRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.service.PostService;
import com.huashidai.lvyouapp.service.PointsService;
import com.huashidai.lvyouapp.util.FileStorageUtil;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态服务实现类
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final PointsService pointsService;
    
    @Override
    public PostResponse createPost(PostCreateRequest request) {
        // 获取当前登录用户（JwtAuthenticationFilter 已将 User 作为 principal 放入 SecurityContext）
        User user = com.huashidai.lvyouapp.util.SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 创建动态
        Post post = new Post();
        post.setContent(request.getContent());
        post.setLocation(request.getLocation());
        post.setLatitude(request.getLatitude());
        post.setLongitude(request.getLongitude());
        post.setType(request.getType());
        post.setStatus(Post.PostStatus.PENDING); // 需要审核
        post.setCreatedAt(LocalDateTime.now());
        post.setUser(user);
        
        Post savedPost = postRepository.save(post);
        
        // 发布帖子获得10积分
        pointsService.earnPointsForPost(user, savedPost.getId());
        
        log.info("用户 {} 发布了新动态，ID: {}，获得10积分", user.getUsername(), savedPost.getId());
        return convertToPostResponse(savedPost);
    }

    @Override
    public PostResponse updatePost(Long id, PostCreateRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("AUTH_REQUIRED", "请先登录后再编辑动态");
        }

        Post post = getEditablePostForCurrentUser(id, currentUser);
        Post.PostStatus originalStatus = post.getStatus();

        post.setContent(request.getContent());
        post.setLocation(request.getLocation());
        post.setLatitude(request.getLatitude());
        post.setLongitude(request.getLongitude());
        post.setType(request.getType());

        post.setStatus(Post.PostStatus.PENDING);

        Post savedPost = postRepository.save(post);
        log.info("用户 {} 编辑动态 {} 成功，状态 {} -> {}",
                currentUser.getUsername(), savedPost.getId(), originalStatus, savedPost.getStatus());
        return convertToPostResponse(savedPost);
    }
    
    @Override
    public void uploadImages(Long postId, List<MultipartFile> files) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("AUTH_REQUIRED", "请先登录后再上传图片");
        }

        Post post = getEditablePostForCurrentUser(postId, currentUser);
        
        // 验证文件
        if (files == null || files.isEmpty()) {
            throw new BusinessException("NO_FILES", "请选择要上传的图片");
        }
        
        // 检查文件数量限制
        if (files.size() > 9) {
            throw new BusinessException("TOO_MANY_FILES", "最多只能上传9张图片，请减少图片数量");
        }
        
        List<PostImage> postImages = new ArrayList<>();
        
        // 处理每个文件
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                continue;
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException("INVALID_FILE_TYPE", "只能上传图片文件，支持JPG、PNG格式");
            }
            
            // 验证文件大小（10MB限制）
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BusinessException("FILE_TOO_LARGE", "图片大小不能超过10MB，请选择较小的文件");
            }
            
            try {
                // 使用文件存储工具保存图片
                String imageUrl = FileStorageUtil.saveFile(file, "posts");
                log.info("图片上传成功: {}", imageUrl);
                
                // 创建PostImage实体并保存到数据库
                PostImage postImage = new PostImage();
                postImage.setPost(post);
                postImage.setImageUrl(imageUrl);
                postImage.setSortOrder(i);
                postImage.setCaption(""); // 可以后续添加图片说明功能
                
                postImages.add(postImage);
                
            } catch (Exception e) {
                log.error("图片上传失败: {}", e.getMessage());
                throw new BusinessException("UPLOAD_FAILED", "图片上传失败，请重试");
            }
        }
        
        // 批量保存图片信息到数据库
        if (!postImages.isEmpty()) {
            try {
                // 先删除该动态的旧图片记录
                postImageRepository.deleteByPostId(postId);
                
                // 批量保存新的图片记录
                postImageRepository.saveAll(postImages);
                
                log.info("动态 {} 的 {} 张图片信息已保存到数据库", postId, postImages.size());
            } catch (Exception e) {
                log.error("保存图片信息到数据库失败: {}", e.getMessage());
                // 如果数据库保存失败，删除已上传的文件
                for (PostImage postImage : postImages) {
                    try {
                        FileStorageUtil.deleteFile(postImage.getImageUrl());
                    } catch (Exception deleteException) {
                        log.error("删除文件失败: {}", deleteException.getMessage());
                    }
                }
                throw new BusinessException("DATABASE_ERROR", "保存图片信息失败，请重试");
            }
        }
        
        log.info("动态 {} 的 {} 张图片上传完成", postId, files.size());
    }
    
    @Override
    public List<String> getPostImages(Long postId) {
        List<PostImage> postImages = postImageRepository.findByPostIdOrderBySortOrder(postId);
        return postImages.stream()
                .map(PostImage::getImageUrl)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public void deletePostImages(Long postId) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException("AUTH_REQUIRED", "请先登录后再删除图片");
            }

            getEditablePostForCurrentUser(postId, currentUser);

            // 获取要删除的图片信息
            List<PostImage> postImages = postImageRepository.findByPostIdOrderBySortOrder(postId);
            
            // 删除数据库记录
            postImageRepository.deleteByPostId(postId);
            
            // 删除物理文件
            for (PostImage postImage : postImages) {
                try {
                    FileStorageUtil.deleteFile(postImage.getImageUrl());
                    log.info("删除图片文件: {}", postImage.getImageUrl());
                } catch (Exception e) {
                    log.error("删除图片文件失败: {}", e.getMessage());
                }
            }
            
            log.info("动态 {} 的所有图片已删除", postId);
        } catch (Exception e) {
            log.error("删除动态图片失败: {}", e.getMessage());
            throw new BusinessException("DELETE_FAILED", "删除图片失败，请重试");
        }
    }
    
    @Override
    public Page<PostResponse> getPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.APPROVED, pageable);
        return posts.map(this::convertToPostResponse);
    }
    
    @Override
    public Page<PostResponse> getPosts(String type, String sort, String startDate, Boolean following, String keyword, Pageable pageable) {
        Sort sortObj = resolvePostSort(sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

        Post.PostType typeFilter = null;
        if (type != null && !type.isBlank()) {
            try {
                typeFilter = Post.PostType.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的动态类型: {}", type);
            }
        }

        LocalDateTime createdAfter = parseIsoDateTime(startDate);

        List<Long> authorIds = null;
        boolean needFollowFilter = Boolean.TRUE.equals(following);
        if (needFollowFilter) {
            User me = SecurityUtils.getCurrentUser();
            if (me == null) {
                return Page.empty(sortedPageable);
            }
            authorIds = userFollowRepository.findFollowingUserIdsByFollowerId(me.getId());
            if (authorIds == null || authorIds.isEmpty()) {
                return Page.empty(sortedPageable);
            }
        }

        final Post.PostType finalType = typeFilter;
        final LocalDateTime finalAfter = createdAfter;
        final List<Long> finalAuthorIds = authorIds;
        final boolean finalNeedFollow = needFollowFilter;
        final String kw = keyword != null && !keyword.isBlank() ? keyword.trim() : null;

        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), Post.PostStatus.APPROVED));
            if (finalType != null) {
                ps.add(cb.equal(root.get("type"), finalType));
            }
            if (finalAfter != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), finalAfter));
            }
            if (kw != null) {
                String pattern = "%" + kw.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("content")), pattern),
                        cb.like(cb.lower(root.get("location")), pattern)));
            }
            if (finalNeedFollow && finalAuthorIds != null) {
                ps.add(root.join("user").get("id").in(finalAuthorIds));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Post> posts = postRepository.findAll(spec, sortedPageable);
        log.info("查询动态列表: type={}, sort={}, startDate={}, following={}, keyword={}, 结果数量={}",
                type, sort, startDate, following, keyword, posts.getTotalElements());
        return posts.map(this::convertToPostResponse);
    }

    private static Sort resolvePostSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        switch (sort.toLowerCase()) {
            case "latest":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular":
            case "hot":
                // Specification + Pageable 走 Criteria，不能使用 Sort 的 NullHandling，否则会抛 UnsupportedOperationException
                return Sort.by(Sort.Direction.DESC, "likeCount", "createdAt");
            case "oldest":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private static LocalDateTime parseIsoDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception e2) {
                return null;
            }
        }
    }
    
    @Override
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, Post.PostStatus.APPROVED, pageable);
        return posts.map(this::convertToPostResponse);
    }

    @Override
    public Page<PostResponse> getMyPosts(String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("AUTH_REQUIRED", "请先登录后查看我的动态");
        }

        Page<Post> posts;
        if (status != null && !status.isBlank()) {
            try {
                Post.PostStatus postStatus = Post.PostStatus.valueOf(status.toUpperCase());
                posts = postRepository.findByUserIdAndStatus(currentUser.getId(), postStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("INVALID_POST_STATUS", "无效的动态状态");
            }
        } else {
            posts = postRepository.findByUserIdAndStatusNot(currentUser.getId(), Post.PostStatus.DELETED, pageable);
        }

        return posts.map(this::convertToPostResponse);
    }
    
    @Override
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        
        return convertToPostResponse(post);
    }
    
    @Override
    public void likePost(Long id) {
        // 获取当前登录用户
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 检查是否已经点赞
        if (postLikeRepository.existsByPostIdAndUserId(id, user.getId())) {
            throw new RuntimeException("已经点赞过了");
        }
        
        // 创建点赞记录
        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);
        postLikeRepository.save(postLike);
        
        // 更新点赞数（兼容历史 null）
        int before = post.getLikeCount() == null ? 0 : post.getLikeCount();
        post.setLikeCount(before + 1);
        postRepository.save(post);
    }
    
    @Override
    public void unlikePost(Long id) {
        // 获取当前登录用户
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 检查是否已经点赞
        if (!postLikeRepository.existsByPostIdAndUserId(id, user.getId())) {
            throw new RuntimeException("还没有点赞");
        }
        
        // 删除点赞记录
        postLikeRepository.deleteByPostIdAndUserId(id, user.getId());
        
        int before = post.getLikeCount() == null ? 0 : post.getLikeCount();
        post.setLikeCount(Math.max(before - 1, 0));
        postRepository.save(post);
    }
    
    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        post.setStatus(Post.PostStatus.DELETED);
        postRepository.save(post);
    }

    private Post getEditablePostForCurrentUser(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("POST_NOT_FOUND", "动态不存在，请刷新页面重试"));

        if (post.getUser() == null || !post.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("POST_EDIT_FORBIDDEN", "只能编辑自己的动态");
        }

        if (post.getStatus() == Post.PostStatus.APPROVED) {
            throw new BusinessException("POST_ALREADY_APPROVED", "审核通过的动态不能再编辑");
        }

        if (post.getStatus() == Post.PostStatus.DELETED) {
            throw new BusinessException("POST_DELETED", "已删除的动态不能编辑");
        }

        return post;
    }
    
    private PostResponse convertToPostResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setLocation(post.getLocation());
        response.setLatitude(post.getLatitude());
        response.setLongitude(post.getLongitude());
        response.setType(post.getType());
        response.setStatus(post.getStatus());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setShareCount(post.getShareCount());
        response.setViewCount(post.getViewCount());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        
        // 检查当前用户是否已点赞
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                response.setIsLiked(postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId()));
            } else {
                response.setIsLiked(false);
            }
        } catch (Exception e) {
            log.warn("检查点赞状态失败: {}", e.getMessage());
            response.setIsLiked(false);
        }
        
        // 设置用户信息（含当前登录用户是否已关注作者）
        if (post.getUser() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(post.getUser().getId());
            userResponse.setUsername(post.getUser().getUsername());
            userResponse.setNickname(post.getUser().getNickname());
            userResponse.setAvatar(post.getUser().getAvatar());
            try {
                User currentUser = SecurityUtils.getCurrentUser();
                Long authorId = post.getUser().getId();
                if (currentUser != null && !currentUser.getId().equals(authorId)) {
                    userResponse.setIsFollowed(
                            userFollowRepository.existsByFollower_IdAndFollowing_Id(currentUser.getId(), authorId));
                } else {
                    userResponse.setIsFollowed(false);
                }
            } catch (Exception e) {
                log.warn("检查关注状态失败: {}", e.getMessage());
                userResponse.setIsFollowed(false);
            }
            response.setUser(userResponse);
        }
        
        // 设置图片列表
        try {
            List<PostImage> postImages = postImageRepository.findByPostIdOrderBySortOrder(post.getId());
            List<String> imageUrls = postImages.stream()
                    .map(PostImage::getImageUrl)
                    .collect(java.util.stream.Collectors.toList());
            response.setImages(imageUrls);
        } catch (Exception e) {
            log.error("获取动态图片失败: {}", e.getMessage());
            response.setImages(new ArrayList<>());
        }
        
        return response;
    }
    
    @Override
    public Page<PostResponse> getLatestPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.APPROVED, pageable);
        return posts.map(this::convertToPostResponse);
    }
    
    @Override
    public Page<PostResponse> getHotPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findPopularPosts(Post.PostStatus.APPROVED, pageable);
        return posts.map(this::convertToPostResponse);
    }
}
