package com.huashidai.lvyouapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 攻略评论请求DTO
 */
@Data
public class GuideCommentRequest {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String content;
    
    private Long parentId; // 父评论ID，用于回复
}
