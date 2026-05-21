package com.huashidai.lvyouapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 跨域配置类
 */
@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsConfig {
    
    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private Boolean allowCredentials = true;
}
