package com.huashidai.lvyouapp.config;

import com.huashidai.lvyouapp.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 允许所有OPTIONS预检请求
                .requestMatchers("/uploads/**").permitAll() // 静态资源优先
                .requestMatchers("/avatars/**").permitAll() // 头像静态资源
                .requestMatchers("/activities/**").permitAll() // 活动图片静态资源
                .requestMatchers("/guides/**").permitAll() // 攻略图片静态资源
                .requestMatchers("/posts/**").permitAll() // 动态图片静态资源
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                // 地图/地点搜索相关接口，供前端调用，不要求登录
                .requestMatchers("/api/map/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                // posts: 仅GET放行，其余需登录
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/activities/**").permitAll()
                .requestMatchers("/api/strategies/**").permitAll() // 允许访问攻略API
                // 景点API: 先配置具体路径，再配置通配符
                .requestMatchers("/api/attractions/favorites").authenticated() // 我的收藏需要登录
                .requestMatchers(HttpMethod.POST, "/api/attractions/*/favorite").authenticated() // 收藏需要登录
                .requestMatchers(HttpMethod.DELETE, "/api/attractions/*/favorite").authenticated() // 取消收藏需要登录
                .requestMatchers(HttpMethod.GET, "/api/attractions/**").permitAll() // GET请求全部放行
                .requestMatchers("/api/expert/apply").authenticated() // 达人申请需要登录
                .requestMatchers("/api/expert/my-application").authenticated() // 查看申请状态需要登录
                .requestMatchers("/api/expert/**").hasAnyRole("EXPERT", "ADMIN") // 其他达人功能需要达人权限
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // 管理员权限
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 预检请求缓存时间（秒）
        configuration.setExposedHeaders(Arrays.asList("*")); // 允许暴露所有响应头
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
