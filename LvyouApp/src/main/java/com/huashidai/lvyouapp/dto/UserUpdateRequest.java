package com.huashidai.lvyouapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新请求DTO
 */
@Data
public class UserUpdateRequest {
    
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;
    
    @Size(max = 100, message = "个人简介长度不能超过100个字符")
    private String bio;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
    
    private String gender;
    
    private String birthday;
    
    @Size(max = 50, message = "所在地长度不能超过50个字符")
    private String location;
    
    @Size(max = 100, message = "兴趣标签长度不能超过100个字符")
    private String interests;
    
    private String avatar;
    
    // 自定义setter方法，处理Base64数据URL
    public void setAvatar(String avatar) {
        if (avatar != null && avatar.startsWith("data:image/")) {
            // 如果是Base64数据URL，则忽略（不存储到数据库）
            this.avatar = null;
        } else {
            this.avatar = avatar;
        }
    }
}
