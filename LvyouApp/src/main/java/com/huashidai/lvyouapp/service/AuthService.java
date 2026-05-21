package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.LoginRequest;
import com.huashidai.lvyouapp.dto.RegisterRequest;
import com.huashidai.lvyouapp.dto.UserResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户注册
     */
    UserResponse register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    Object login(LoginRequest request);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser();
}
