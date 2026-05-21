package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户响应DTO
 */
@Data
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private String gender;
    private Integer age;
    private String location;
    private String bio;
    private String birthday;
    private String interests;
    private User.UserRole role;
    private User.UserStatus status;
    private Integer points;
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
    private Integer activitiesCount;
    private Integer strategiesCount;
    private Boolean isVerified;
    /** 当前登录用户是否已关注该用户（未登录或未在帖子上下文中填充时为 null/false） */
    private Boolean isFollowed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
