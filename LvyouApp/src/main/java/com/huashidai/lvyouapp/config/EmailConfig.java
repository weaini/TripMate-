package com.huashidai.lvyouapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件配置类
 */
@Configuration
@ConfigurationProperties(prefix = "email.verification")
@Data
public class EmailConfig {
    
    private Integer codeLength = 6;
    private Integer expireMinutes = 5;
    private Integer sendIntervalSeconds = 60;
}
