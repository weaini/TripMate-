# 旅游小程序增强计划

## 一、问题诊断

### 当前缺失的旅游特色功能
1. ❌ **地理位置服务**：虽有location字段，但无地图、路线规划、附近推荐
2. ❌ **行程规划**：无行程单、日程管理、预算统计
3. ❌ **目的地内容**：无目的地详情页、景点/美食/住宿推荐、天气信息
4. ❌ **旅游工具**：无汇率、语言翻译、紧急联系、离线地图

## 二、新增模块规划

### 模块1：目的地系统（Destination）
**功能**：
- 目的地详情页（介绍、攻略、活动、用户动态聚合）
- 景点/美食/住宿推荐（POI数据）
- 目的地搜索与筛选
- 热门目的地榜单

**数据模型**：
```sql
CREATE TABLE destinations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    country VARCHAR(50),
    city VARCHAR(50),
    cover_image VARCHAR(500),
    view_count INT DEFAULT 0,
    activity_count INT DEFAULT 0,
    guide_count INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE poi_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    destination_id BIGINT,
    name VARCHAR(200),
    type ENUM('ATTRACTION', 'RESTAURANT', 'HOTEL', 'SHOPPING'),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    rating DECIMAL(3,2),
    description TEXT,
    FOREIGN KEY (destination_id) REFERENCES destinations(id)
);
```

### 模块2：行程规划（TravelPlan）
**功能**：
- 创建/编辑行程（日期、地点、活动、费用）
- 行程分享、复制、协作编辑
- 行程统计（天数、预算、路线可视化）

**数据模型**：
```sql
CREATE TABLE travel_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    description TEXT,
    start_date DATE,
    end_date DATE,
    budget DECIMAL(10,2),
    is_public BOOLEAN DEFAULT false,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE plan_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    day_number INT,
    destination_id BIGINT,
    activity_id BIGINT,
    start_time DATETIME,
    end_time DATETIME,
    notes TEXT,
    cost DECIMAL(10,2),
    sort_order INT,
    FOREIGN KEY (plan_id) REFERENCES travel_plans(id)
);
```

### 模块3：地图与位置服务
**前端集成**：
- 高德地图/百度地图API
- 附近推荐（活动、用户、景点）
- 路线规划与导航

### 模块4：旅游工具集
**功能**：
- 天气查询（目的地天气）
- 汇率转换
- 紧急联系（目的地紧急电话）
- 离线攻略下载

## 三、Redis使用方案

### 1. 热点数据缓存
```java
// 目的地信息缓存（24小时）
redisService.set("cache:destination:" + id, destination, 24, TimeUnit.HOURS);

// 活动列表缓存（1小时）
redisService.set("cache:activities:page:" + page, activities, 1, TimeUnit.HOURS);
```

### 2. 实时计数（异步写入DB）
```java
// 浏览量实时计数
redisService.increment("count:view:activity:" + activityId);
// 每小时批量同步到数据库
```

### 3. 限流与防刷
```java
// 发布频率限制（1分钟最多3次）
String key = "ratelimit:post:" + userId;
if (!checkRateLimit(key, 3, 60)) {
    throw new BusinessException("发布过于频繁");
}
```

### 4. 排行榜
```java
// 热门目的地排行榜（ZSET）
redisTemplate.opsForZSet().add("rank:destination:hot", destinationId, score);
Set<Object> top10 = redisTemplate.opsForZSet().reverseRange("rank:destination:hot", 0, 9);
```

### 5. 用户状态
```java
// 在线用户（5分钟过期）
redisService.set("user:online:" + userId, "1", 5, TimeUnit.MINUTES);

// 用户位置（用于附近推荐，30分钟过期）
redisService.set("user:location:" + userId, location, 30, TimeUnit.MINUTES);
```

### 6. 消息队列
```java
// 通知推送队列
redisTemplate.opsForList().rightPush("queue:notifications", notification);
```

## 四、完善思路

### 阶段1：核心旅游功能（2周）
1. ✅ 目的地模块（详情页、搜索、榜单）
2. ✅ 行程规划（创建、编辑、分享）
3. ✅ 地图集成（附近推荐、路线规划）

### 阶段2：内容增强（1周）
1. ✅ 目的地攻略聚合
2. ✅ 智能推荐算法
3. ✅ 旅游工具集

### 阶段3：性能优化（1周）
1. ✅ Redis缓存全面应用
2. ✅ 数据库查询优化
3. ✅ 图片CDN加速

### 阶段4：社交增强（1周）
1. ✅ 同城/同目的地匹配
2. ✅ 旅行足迹
3. ✅ 成就系统

## 五、技术实现要点

### Redis缓存策略
- **缓存穿透**：布隆过滤器 + 空值缓存
- **缓存击穿**：分布式锁 + 热点数据永不过期
- **缓存雪崩**：随机TTL + 多级缓存

### 地理位置服务
- **附近搜索**：Redis GEO 或 Elasticsearch
- **路线规划**：第三方地图API（高德/百度）
- **距离计算**：Haversine公式或Redis GEO距离

### 推荐算法
- **协同过滤**：基于用户行为
- **内容推荐**：基于目的地标签
- **热门推荐**：基于Redis ZSET排行榜


