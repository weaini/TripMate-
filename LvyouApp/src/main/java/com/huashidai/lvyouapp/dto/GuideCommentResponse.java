package com.huashidai.lvyouapp.dto;

import lombok.Data;
import com.huashidai.lvyouapp.entity.GuideComment;
import com.huashidai.lvyouapp.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 攻略评论响应DTO
 */
@Data
public class GuideCommentResponse {
    
    private Long id;
    private String content;
    private Long guideId;
    private UserInfo user;
    private Long parentId;
    private List<GuideCommentResponse> replies;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isLiked;
    private Boolean isAuthor;
    
    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        
        public static UserInfo fromUser(User user) {
            if (user == null) return null;
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            return userInfo;
        }
    }
    
    /**
     * 从GuideComment实体转换为GuideCommentResponse
     */
    public static GuideCommentResponse fromGuideComment(GuideComment comment) {
        if (comment == null) return null;
        
        GuideCommentResponse response = new GuideCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setGuideId(comment.getGuide().getId());
        response.setUser(UserInfo.fromUser(comment.getUser()));
        response.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        response.setLikeCount(comment.getLikeCount());
        response.setStatus(comment.getStatus().name());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setIsLiked(false); // 默认值，需要根据当前用户状态设置
        response.setIsAuthor(false); // 默认值，需要根据当前用户状态设置
        
        return response;
    }
}
