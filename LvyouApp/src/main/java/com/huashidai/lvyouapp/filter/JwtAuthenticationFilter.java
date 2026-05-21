package com.huashidai.lvyouapp.filter;

import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // JWT Token在请求头中的格式为 "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                logger.error("Unable to get JWT Token or JWT Token has expired");
            }
        }
        
        // 验证token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("JWT认证过滤器: 开始验证token，用户名: " + username);
            if (jwtUtil.validateToken(jwtToken, username)) {
                logger.info("JWT认证过滤器: Token验证成功，开始获取用户信息");
                // 从数据库获取完整的用户信息
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    logger.info("JWT认证过滤器: 找到用户: " + user.getUsername() + " (ID: " + user.getId() + ")");
                    
                    // 根据用户角色设置权限
                    String role = "ROLE_" + user.getRole().name();
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(authority);
                    
                    logger.info("JWT认证过滤器: 设置用户权限: " + role);
                    
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("JWT认证过滤器: SecurityContext设置完成");
                } else {
                    logger.warn("JWT认证过滤器: 用户不存在: " + username);
                }
            } else {
                logger.warn("JWT认证过滤器: Token验证失败");
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
