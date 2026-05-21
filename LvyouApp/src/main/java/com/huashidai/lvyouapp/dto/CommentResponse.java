package com.huashidai.lvyouapp.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 */
@Data
public class CommentResponse {
    
    private Long id;
    private UserResponse user;
    private String content;
    private Long parentId;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 回复列表
    private List<CommentResponse> replies;
    
    // 是否已点赞
    private Boolean isLiked;
}