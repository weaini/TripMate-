package com.huashidai.lvyouapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 攻略创建/更新请求DTO
 */
@Data
public class GuideRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;
    
    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;
    
    private String coverImage;
    
    @NotNull(message = "攻略类型不能为空")
    private String type;
    
    private String status = "PUBLISHED";
}
