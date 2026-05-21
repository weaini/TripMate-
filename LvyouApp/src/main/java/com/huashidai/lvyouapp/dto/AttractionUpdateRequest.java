package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Attraction;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 景点更新请求DTO
 */
@Data
public class AttractionUpdateRequest {
    
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
    
    private Attraction.AttractionLevel level;
    
    private BigDecimal ticketPrice;
    
    private String openingHours;
    
    private String contactPhone;
    
    private String website;
    
    private String tips;
    
    private Integer rating;
    
    private Attraction.AttractionStatus status;
    
    private List<String> imageUrls; // 图片URL列表
}

