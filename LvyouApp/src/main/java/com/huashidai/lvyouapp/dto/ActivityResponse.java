package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Activity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动响应DTO
 */
@Data
public class ActivityResponse {
    
    private Long id;
    private UserResponse organizer;
    private String title;
    private String description;
    private String destination;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    /** 待审核报名人数（仅活动详情等场景返回） */
    private Integer pendingParticipantCount;
    private BigDecimal cost;
    private Activity.ActivityType type;
    private Activity.ActivityStatus status;
    private Activity.ActivityLevel level;
    private String requirements;
    private String contactInfo;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images;
    private Boolean isJoined;
    private Boolean isLiked;
}