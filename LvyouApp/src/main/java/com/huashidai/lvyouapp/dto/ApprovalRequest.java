package com.huashidai.lvyouapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 审核请求DTO
 */
@Data
public class ApprovalRequest {
    
    @NotBlank(message = "审核备注不能为空")
    private String note;
    
    private String reason;
}
