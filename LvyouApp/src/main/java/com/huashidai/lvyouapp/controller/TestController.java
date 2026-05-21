package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 创建测试用户
     */
    @PostMapping("/create-users")
    public ResponseEntity<Map<String, Object>> createTestUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查是否已有用户
            long userCount = userRepository.count();
            if (userCount > 0) {
                response.put("success", true);
                response.put("message", "用户已存在，跳过创建");
                response.put("userCount", userCount);
                return ResponseEntity.ok(response);
            }
            
            // 创建测试用户
            createUser("admin", "admin@lvyou.com", "系统管理员", User.UserRole.ADMIN);
            createUser("testuser", "test@lvyou.com", "测试用户", User.UserRole.TOURIST);
            createUser("traveler", "traveler@lvyou.com", "旅行达人", User.UserRole.EXPERT);
            createUser("hiker", "hiker@lvyou.com", "登山爱好者", User.UserRole.TOURIST);
            
            response.put("success", true);
            response.put("message", "测试用户创建成功");
            response.put("userCount", userRepository.count());
            
        } catch (Exception e) {
            log.error("创建测试用户失败", e);
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<User> users = userRepository.findAll();
            response.put("success", true);
            response.put("users", users);
            response.put("count", users.size());
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 测试接口
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "后端服务正常运行");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 测试用户登录
     */
    @PostMapping("/test-login")
    public ResponseEntity<Map<String, Object>> testLogin(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                user = userRepository.findByEmail(username).orElse(null);
            }
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }
            
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            
            response.put("success", passwordMatches);
            response.put("message", passwordMatches ? "登录成功" : "密码错误");
            response.put("user", user);
            
        } catch (Exception e) {
            log.error("测试登录失败", e);
            response.put("success", false);
            response.put("message", "测试失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    private void createUser(String username, String email, String nickname, User.UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setIsVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        
        if (role == User.UserRole.TOURIST) {
            user.setPoints(100);
            user.setFollowersCount(0);
            user.setFollowingCount(0);
        } else if (role == User.UserRole.EXPERT) {
            user.setPoints(500);
            user.setFollowersCount(0);
            user.setFollowingCount(0);
        }
        
        userRepository.save(user);
        log.info("创建用户: {} ({})", username, nickname);
    }
}


