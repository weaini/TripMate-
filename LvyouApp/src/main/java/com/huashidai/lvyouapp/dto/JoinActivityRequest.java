package com.huashidai.lvyouapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 报名活动请求DTO
 */
@Data
public class JoinActivityRequest {
    
    @NotNull(message = "申请说明不能为空")
    private String applicationNote;
}
