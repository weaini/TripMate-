package com.huashidai.lvyouapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 达人申请响应DTO
 */
@Data
public class ExpertApplicationResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String reason;
    private String bio;
    private String expertise;
    private String contactInfo;
    private String status;
    private String adminNote;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
}
