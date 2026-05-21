package com.huashidai.lvyouapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 行程创建请求DTO
 */
@Data
public class TravelPlanCreateRequest {
    
    @NotBlank(message = "行程标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    private Double budget;
    
    private Boolean isPublic = false;
    
    private List<PlanItemRequest> items;
    
    @Data
    public static class PlanItemRequest {
        private Integer dayNumber;
        private Long destinationId;
        private Long activityId;
        private String startTime;
        private String endTime;
        private String notes;
        private Double cost;
    }
}

