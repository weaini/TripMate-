package com.huashidai.lvyouapp.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final RateLimitInterceptor rateLimitInterceptor;

    @Value("${app.upload-dir:${user.home}/.lvyou-app/uploads}")
    private String uploadRootDir;
    
    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRootPath = Paths.get(uploadRootDir).toAbsolutePath().normalize();
        Path legacyUploadRoot = Paths.get(System.getProperty("user.dir"), "src", "main", "webapp", "uploads")
                .toAbsolutePath()
                .normalize();

        // 添加静态资源映射 - 优先使用独立上传目录，兼容当前项目旧目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        toResourceLocation(uploadRootPath) + "/",
                        toResourceLocation(legacyUploadRoot) + "/"
                );

        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(
                        toResourceLocation(uploadRootPath.resolve("avatars")) + "/",
                        toResourceLocation(legacyUploadRoot.resolve("avatars")) + "/"
                );

        registry.addResourceHandler("/activities/**")
                .addResourceLocations(
                        toResourceLocation(uploadRootPath.resolve("activities")) + "/",
                        toResourceLocation(legacyUploadRoot.resolve("activities")) + "/"
                );

        registry.addResourceHandler("/guides/**")
                .addResourceLocations(
                        toResourceLocation(uploadRootPath.resolve("guides")) + "/",
                        toResourceLocation(legacyUploadRoot.resolve("guides")) + "/"
                );

        registry.addResourceHandler("/posts/**")
                .addResourceLocations(
                        toResourceLocation(uploadRootPath.resolve("posts")) + "/",
                        toResourceLocation(legacyUploadRoot.resolve("posts")) + "/"
                );

        ensureDirExists(uploadRootPath);
        ensureDirExists(uploadRootPath.resolve("avatars"));
        ensureDirExists(uploadRootPath.resolve("posts"));
        ensureDirExists(uploadRootPath.resolve("guides"));
        ensureDirExists(uploadRootPath.resolve("activities"));
        ensureDirExists(uploadRootPath.resolve("attractions"));
    }

    private void ensureDirExists(Path dirPath) {
        File dir = dirPath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private String toResourceLocation(Path path) {
        return "file:" + path.toString().replace("\\", "/");
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }
}
