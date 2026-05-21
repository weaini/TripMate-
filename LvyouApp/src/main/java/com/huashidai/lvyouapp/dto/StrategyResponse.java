package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Strategy;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 攻略响应DTO
 */
@Data
public class StrategyResponse {
    
    private Long id;
    private UserResponse author;
    private String title;
    private String content;
    private String destination;
    private String summary;
    private BigDecimal budget;
    private Integer duration;
    private Strategy.StrategyType type;
    private Strategy.StrategyStatus status;
    private Strategy.StrategyLevel level;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer rewardPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images;
    private Boolean isLiked;
}
