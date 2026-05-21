package com.huashidai.lvyouapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 邮件服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final RedisService redisService;
    
    @Value("${email.verification.code-length}")
    private Integer codeLength;
    
    @Value("${email.verification.expire-minutes}")
    private Integer expireMinutes;
    
    @Value("${email.verification.send-interval-seconds}")
    private Integer sendIntervalSeconds;
    
    /**
     * 发送验证码邮件
     */
    public void sendVerificationCode(String email) {
        // 检查发送间隔
        String intervalKey = "email:interval:" + email;
        if (redisService.hasKey(intervalKey)) {
            throw new RuntimeException("请等待" + sendIntervalSeconds + "秒后再发送验证码");
        }
        
        // 生成验证码
        String code = generateVerificationCode();
        
        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("旅游社交平台 - 邮箱验证码");
        message.setText("您的验证码是：" + code + "，有效期" + expireMinutes + "分钟，请勿泄露给他人。");
        
        try {
            mailSender.send(message);
            log.info("验证码邮件发送成功，邮箱：{}", email);
        } catch (Exception e) {
            log.error("验证码邮件发送失败，邮箱：{}", email, e);
            throw new RuntimeException("邮件发送失败");
        }
        
        // 存储验证码到Redis
        String codeKey = "email:code:" + email;
        redisService.set(codeKey, code, expireMinutes * 60L, java.util.concurrent.TimeUnit.SECONDS);
        
        // 设置发送间隔
        redisService.set(intervalKey, "1", sendIntervalSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    /**
     * 验证邮箱验证码
     */
    public boolean verifyCode(String email, String code) {
        String codeKey = "email:code:" + email;
        String storedCode = (String) redisService.get(codeKey);
        
        if (storedCode == null) {
            return false;
        }
        
        if (storedCode.equals(code)) {
            // 验证成功后删除验证码
            redisService.delete(codeKey);
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
}
