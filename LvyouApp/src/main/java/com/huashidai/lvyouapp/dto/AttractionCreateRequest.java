package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Attraction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 景点创建请求DTO
 */
@Data
public class AttractionCreateRequest {
    
    @NotBlank(message = "景点名称不能为空")
    private String name;
    
    private String description;
    
    private String introduction;
    
    private String country;
    
    private String province;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    private String address;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private String coverImage;
    
    @NotNull(message = "景点类型不能为空")
    private Attraction.AttractionType type;
    
    private Attraction.AttractionLevel level = Attraction.AttractionLevel.UNRATED;
    
    private BigDecimal ticketPrice;
    
    private String openingHours;
    
    private String contactPhone;
    
    private String website;
    
    private String tips;
    
    private Integer rating = 0;
    
    private List<String> imageUrls; // 图片URL列表
}

