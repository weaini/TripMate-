package com.huashidai.lvyouapp.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 行程响应DTO
 */
@Data
public class TravelPlanResponse {
    
    private Long id;
    
    private String title;
    
    private String description;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Double budget;
    
    private Boolean isPublic;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private UserResponse creator;
    
    private List<PlanItemResponse> items;
    
    private LocalDateTime createdAt;
    
    @Data
    public static class PlanItemResponse {
        private Long id;
        private Integer dayNumber;
        private Long destinationId;
        private String destinationName;
        private Long activityId;
        private String activityTitle;
        private String startTime;
        private String endTime;
        private String notes;
        private Double cost;
    }
}

