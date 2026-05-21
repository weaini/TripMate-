package com.huashidai.lvyouapp.util;

import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据库初始化工具
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化数据库用户数据...");
        
        // 检查是否已有用户数据
        if (userRepository.count() > 0) {
            log.info("数据库中已有用户数据，跳过初始化");
            return;
        }
        
        // 创建测试用户
        createTestUsers();
        
        log.info("数据库用户数据初始化完成");
    }
    
    private void createTestUsers() {
        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@lvyou.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNickname("系统管理员");
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus(User.UserStatus.ACTIVE);
        admin.setIsVerified(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
        log.info("创建管理员用户: admin");
        
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@lvyou.com");
        testUser.setPassword(passwordEncoder.encode("123456"));
        testUser.setNickname("测试用户");
        testUser.setRole(User.UserRole.TOURIST);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setIsVerified(true);
        testUser.setPoints(100);
        testUser.setFollowersCount(0);
        testUser.setFollowingCount(0);
        testUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(testUser);
        log.info("创建测试用户: testuser");
        
        // 创建旅行达人
        User traveler = new User();
        traveler.setUsername("traveler");
        traveler.setEmail("traveler@lvyou.com");
        traveler.setPassword(passwordEncoder.encode("123456"));
        traveler.setNickname("旅行达人");
        traveler.setRole(User.UserRole.EXPERT);
        traveler.setStatus(User.UserStatus.ACTIVE);
        traveler.setIsVerified(true);
        traveler.setPoints(500);
        traveler.setFollowersCount(0);
        traveler.setFollowingCount(0);
        traveler.setCreatedAt(LocalDateTime.now());
        userRepository.save(traveler);
        log.info("创建旅行达人: traveler");
        
        // 创建登山爱好者
        User hiker = new User();
        hiker.setUsername("hiker");
        hiker.setEmail("hiker@lvyou.com");
        hiker.setPassword(passwordEncoder.encode("123456"));
        hiker.setNickname("登山爱好者");
        hiker.setRole(User.UserRole.TOURIST);
        hiker.setStatus(User.UserStatus.ACTIVE);
        hiker.setIsVerified(true);
        hiker.setPoints(200);
        hiker.setFollowersCount(0);
        hiker.setFollowingCount(0);
        hiker.setCreatedAt(LocalDateTime.now());
        userRepository.save(hiker);
        log.info("创建登山爱好者: hiker");
    }
}
