package com.huashidai.lvyouapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 达人申请请求DTO
 */
@Data
public class ExpertApplicationRequest {
    
    @NotBlank(message = "申请理由不能为空")
    @Size(max = 1000, message = "申请理由不能超过1000字符")
    private String reason;
    
    @NotBlank(message = "个人简介不能为空")
    @Size(max = 500, message = "个人简介不能超过500字符")
    private String bio;
    
    @Size(max = 200, message = "专业领域不能超过200字符")
    private String expertise;
    
    @Size(max = 200, message = "联系方式不能超过200字符")
    private String contactInfo;
}
