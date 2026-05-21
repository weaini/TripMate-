package com.huashidai.lvyouapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 拒绝参与者请求DTO
 */
@Data
public class RejectParticipantRequest {
    
    @NotBlank(message = "拒绝原因不能为空")
    private String reason;
}
