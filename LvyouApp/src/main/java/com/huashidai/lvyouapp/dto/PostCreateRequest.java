package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 动态创建请求DTO
 */
@Data
public class PostCreateRequest {
    
    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000个字符")
    private String content;
    
    private String location;
    
    private String latitude;
    
    private String longitude;
    
    private Post.PostType type = Post.PostType.DYNAMIC;
}
