package com.huashidai.lvyouapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 参与者响应DTO
 */
@Data
public class ParticipantResponse {
    
    private Long id;
    private UserResponse user;
    private String status;
    private String applicationNote;
    private String rejectionReason;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
}
