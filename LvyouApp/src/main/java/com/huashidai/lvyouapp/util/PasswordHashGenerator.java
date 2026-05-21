package com.huashidai.lvyouapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成器
 * 用于生成正确的密码哈希值
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hash = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        
        // 验证哈希是否正确
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + matches);
        
        // 测试其他密码
        String[] testPasswords = {"password", "secret", "admin123"};
        for (String testPassword : testPasswords) {
            String testHash = encoder.encode(testPassword);
            boolean testMatches = encoder.matches(testPassword, testHash);
            System.out.println("密码: " + testPassword + " -> 哈希: " + testHash + " -> 验证: " + testMatches);
        }
    }
}
