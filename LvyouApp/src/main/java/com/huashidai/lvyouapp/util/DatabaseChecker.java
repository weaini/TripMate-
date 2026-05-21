package com.huashidai.lvyouapp.util;

import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库检查工具
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseChecker implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== 数据库用户数据检查 ===");
        
        // 检查用户总数
        long userCount = userRepository.count();
        log.info("数据库中的用户总数: {}", userCount);
        
        if (userCount == 0) {
            log.warn("数据库中没有用户数据！");
            return;
        }
        
        // 列出所有用户
        List<User> allUsers = userRepository.findAll();
        log.info("所有用户列表:");
        for (User user : allUsers) {
            log.info("用户: {} (ID: {}, 邮箱: {}, 角色: {}, 状态: {})", 
                user.getUsername(), user.getId(), user.getEmail(), user.getRole(), user.getStatus());
        }
        
        // 测试特定用户
        testUserLogin("testuser", "123456");
        testUserLogin("admin", "123456");
        testUserLogin("traveler", "123456");
        
        log.info("=== 数据库检查完成 ===");
    }
    
    private void testUserLogin(String username, String password) {
        try {
            log.info("测试用户登录: {}", username);
            
            // 查找用户
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                log.warn("用户不存在: {}", username);
                return;
            }
            
            log.info("找到用户: {} (ID: {})", user.getUsername(), user.getId());
            log.info("用户密码哈希: {}", user.getPassword());
            
            // 测试密码验证
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            log.info("密码验证结果: {}", passwordMatches);
            
            if (passwordMatches) {
                log.info("✅ 用户 {} 登录测试成功", username);
            } else {
                log.warn("❌ 用户 {} 密码验证失败", username);
            }
            
        } catch (Exception e) {
            log.error("测试用户登录时出错: {}", e.getMessage(), e);
        }
    }
}


