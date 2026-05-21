package com.huashidai.lvyouapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static class Counter {
        long windowStart;
        int count;
    }

    private final Map<String, Counter> ipCounter = new ConcurrentHashMap<>();
    private final long windowMillis = 1000; // 1秒窗口
    private final int maxRequests = 20;     // 每秒最多20次

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        long now = Instant.now().toEpochMilli();
        Counter c = ipCounter.computeIfAbsent(ip, k -> new Counter());
        synchronized (c) {
            if (now - c.windowStart > windowMillis) {
                c.windowStart = now;
                c.count = 0;
            }
            c.count++;
            if (c.count > maxRequests) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"code\":\"TOO_MANY_REQUESTS\",\"message\":\"请求过于频繁，请稍后再试\"}");
                return false;
            }
        }
        return true;
    }
}

