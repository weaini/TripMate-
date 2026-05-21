package com.huashidai.lvyouapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论创建请求DTO
 */
@Data
public class CommentCreateRequest {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;
    
    private Long parentId; // 父评论ID，用于回复
}
