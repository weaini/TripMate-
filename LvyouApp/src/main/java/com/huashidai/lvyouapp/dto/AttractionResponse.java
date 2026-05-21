package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Attraction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 景点响应DTO
 */
@Data
public class AttractionResponse {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String introduction;
    
    private String country;
    
    private String province;
    
    private String city;
    
    private String address;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private String coverImage;
    
    private Attraction.AttractionType type;
    
    private String typeName; // 类型中文名称
    
    private Attraction.AttractionLevel level;
    
    private String levelName; // 等级中文名称
    
    private BigDecimal ticketPrice;
    
    private String openingHours;
    
    private String contactPhone;
    
    private String website;
    
    private String tips;
    
    private Integer viewCount;
    
    private Integer favoriteCount;
    
    private Integer rating;
    
    private Boolean isFavorited; // 当前用户是否已收藏
    
    private Attraction.AttractionStatus status;
    
    private List<AttractionImageResponse> images;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Data
    public static class AttractionImageResponse {
        private Long id;
        private String imageUrl;
        private String caption;
        private Integer sortOrder;
    }
}

