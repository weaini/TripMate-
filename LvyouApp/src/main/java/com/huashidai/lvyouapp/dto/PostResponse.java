package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态响应DTO
 */
@Data
public class PostResponse {
    
    private Long id;
    private UserResponse user;
    private String content;
    private String location;
    private String latitude;
    private String longitude;
    private Post.PostType type;
    private Post.PostStatus status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images;
    private Boolean isLiked;
}
