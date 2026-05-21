package com.huashidai.lvyouapp.dto;

import lombok.Data;
import com.huashidai.lvyouapp.entity.Guide;
import com.huashidai.lvyouapp.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 攻略响应DTO
 */
@Data
public class GuideResponse {
    
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String type;
    private String typeDisplayName;
    private String status;
    private String statusDisplayName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private UserInfo author;
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
     * 从Guide实体转换为GuideResponse
     */
    public static GuideResponse fromGuide(Guide guide) {
        if (guide == null) return null;
        
        GuideResponse response = new GuideResponse();
        response.setId(guide.getId());
        response.setTitle(guide.getTitle());
        response.setContent(guide.getContent());
        response.setSummary(guide.getSummary());
        response.setCoverImage(guide.getCoverImage());
        response.setType(guide.getType().name());
        response.setTypeDisplayName(guide.getType().getDisplayName());
        response.setStatus(guide.getStatus().name());
        response.setStatusDisplayName(guide.getStatus().getDisplayName());
        response.setViewCount(guide.getViewCount());
        response.setLikeCount(guide.getLikeCount());
        response.setCommentCount(guide.getCommentCount());
        response.setAuthor(UserInfo.fromUser(guide.getAuthor()));
        response.setCreatedAt(guide.getCreatedAt());
        response.setUpdatedAt(guide.getUpdatedAt());
        response.setIsLiked(false); // 默认值，需要根据当前用户状态设置
        response.setIsAuthor(false); // 默认值，需要根据当前用户状态设置
        
        return response;
    }
}
