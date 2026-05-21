package com.huashidai.lvyouapp.dto;

import lombok.Data;

/**
 * 管理员统计数据响应DTO
 */
@Data
public class AdminStatisticsResponse {
    
    private Long totalUsers;
    private Long totalPosts;
    private Long totalActivities;
    private Long totalStrategies;
    private Long pendingPosts;
    private Long pendingActivities;
    private Long pendingExpertApplications;
    private Long todayNewUsers;
    private Long todayNewPosts;
    private Long todayNewActivities;
}
