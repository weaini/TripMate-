package com.huashidai.lvyouapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 拒绝请求DTO
 */
@Data
public class RejectionRequest {
    
    @NotBlank(message = "拒绝原因不能为空")
    private String reason;
    
    private String note;
}
