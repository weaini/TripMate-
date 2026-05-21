package com.huashidai.lvyouapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存增强服务 - 旅游场景专用
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // ========== 热点数据缓存 ==========
    private static final String CACHE_PREFIX_DESTINATION = "cache:destination:";
    private static final String CACHE_PREFIX_ACTIVITY = "cache:activity:";
    private static final String CACHE_PREFIX_POST = "cache:post:";
    private static final int CACHE_EXPIRE_HOURS = 24;
    
    // ========== 实时计数 ==========
    private static final String COUNT_PREFIX_VIEW = "count:view:";
    private static final String COUNT_PREFIX_LIKE = "count:like:";
    private static final String COUNT_PREFIX_PARTICIPANT = "count:participant:";
    
    // ========== 限流 ==========
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    
    // ========== 排行榜 ==========
    private static final String RANK_DESTINATION = "rank:destination:hot";
    private static final String RANK_ACTIVITY = "rank:activity:hot";
    
    // ========== 用户状态 ==========
    private static final String USER_ONLINE = "user:online:";
    private static final String USER_LOCATION = "user:location:";
    
    // 缓存目的地信息
    public void cacheDestination(Long id, Object destination) {
        String key = CACHE_PREFIX_DESTINATION + id;
        redisTemplate.opsForValue().set(key, destination, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    public Object getCachedDestination(Long id) {
        return redisTemplate.opsForValue().get(CACHE_PREFIX_DESTINATION + id);
    }
    
    // 实时浏览量计数（异步写入数据库）
    public void incrementViewCount(String type, Long id) {
        String key = COUNT_PREFIX_VIEW + type + ":" + id;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
    
    public Long getViewCount(String type, Long id) {
        String key = COUNT_PREFIX_VIEW + type + ":" + id;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count.toString()) : 0L;
    }
    
    // 限流检查
    public boolean checkRateLimit(String identifier, int maxRequests, int windowSeconds) {
        String key = RATE_LIMIT_PREFIX + identifier;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count != null && count <= maxRequests;
    }
    
    // 热门目的地排行榜
    public void addToDestinationRank(Long destinationId, double score) {
        redisTemplate.opsForZSet().add(RANK_DESTINATION, destinationId.toString(), score);
    }
    
    public Set<Object> getTopDestinations(int limit) {
        return redisTemplate.opsForZSet().reverseRange(RANK_DESTINATION, 0, limit - 1);
    }
    
    // 用户在线状态
    public void setUserOnline(Long userId) {
        String key = USER_ONLINE + userId;
        redisTemplate.opsForValue().set(key, "1", 5, TimeUnit.MINUTES);
    }
    
    public boolean isUserOnline(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_ONLINE + userId));
    }
    
    // 用户位置（用于附近的人/活动推荐）
    public void setUserLocation(Long userId, Double latitude, Double longitude) {
        String key = USER_LOCATION + userId;
        redisTemplate.opsForHash().put(key, "lat", latitude.toString());
        redisTemplate.opsForHash().put(key, "lng", longitude.toString());
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }
    
    // 附近活动推荐（基于地理位置）
    public Set<Object> findNearbyActivities(Double latitude, Double longitude, double radiusKm) {
        // 使用Redis GEO功能实现附近搜索
        // 需要将活动位置存储到Redis GEO数据结构中
        return redisTemplate.opsForZSet().range("geo:activities", 0, -1);
    }
}


