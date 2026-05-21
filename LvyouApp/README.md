# 旅游社交平台后端项目

## 项目简介

这是一个基于Spring Boot的旅游社交平台后端项目，支持游客、旅游达人和平台管理员三种角色，提供社交旅游、内容分享、活动组织等功能。

## 技术栈

- **后端框架**: Spring Boot 3.5.6
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **文件存储**: MinIO
- **安全认证**: Spring Security + JWT
- **ORM框架**: Spring Data JPA
- **构建工具**: Maven
- **Java版本**: 17

## 核心功能

### A. 游客功能
1. **社交旅游**
   - 结伴同行：发布结伴信息，注明目的地、时间，寻找同行伙伴
   - 活动参与：报名其他用户组织的旅游活动，如徒步、摄影采风

2. **内容分享**
   - 实时动态：旅行中发布图文动态，标记位置，与好友互动
   - 攻略创作：撰写详细攻略，标注花费、避坑点，获得平台奖励

### B. 旅游达人功能
1. **活动组织**
   - 发起活动：设计活动主题、行程，设置报名门槛、费用
   - 成员管理：审核报名人员，建立群聊，发布活动通知

2. **粉丝运营**
   - 回复互动：回复粉丝评论，解答旅游问题，积累人气
   - 数据查看：查看活动参与率、攻略阅读量，优化内容

### C. 平台管理员功能
1. **内容监管**
   - 动态审核：过滤违规图文，处理用户举报的不良内容
   - 活动审核：检查活动安全性，禁止高危、不合规活动

2. **激励设置**
   - 积分体系：用户分享、参与活动获得积分，兑换旅游周边
   - 达人认证：为优质内容创作者发放"旅游达人"标识

## 数据库设计

### 核心表结构
- **users**: 用户表
- **posts**: 动态表
- **activities**: 活动表
- **strategies**: 攻略表
- **activity_participants**: 活动参与者表
- **comments**: 评论表
- **post_likes**: 动态点赞表
- **user_follows**: 用户关注表
- **notifications**: 通知表
- **reports**: 举报表
- **points_records**: 积分记录表

## 环境配置

### 1. 数据库配置
```properties
# MySQL配置
spring.datasource.url=jdbc:mysql://localhost:3306/lvyou_app?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
```

### 2. Redis配置
```properties
# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
```

### 3. MinIO配置
```properties
# MinIO配置
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=lvyou-app
```

## 快速开始

### 1. 环境准备
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- MinIO Server

### 2. 数据库初始化
```sql
-- 执行初始化脚本
source src/main/resources/sql/init.sql
```

### 3. 启动服务
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 4. 访问接口
- 应用地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## API接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/me` - 获取当前用户信息

### 动态接口
- `POST /api/posts` - 发布动态
- `GET /api/posts` - 获取动态列表
- `GET /api/posts/{id}` - 获取动态详情
- `POST /api/posts/{id}/like` - 点赞动态
- `DELETE /api/posts/{id}/like` - 取消点赞

### 活动接口
- `POST /api/activities` - 创建活动
- `GET /api/activities` - 获取活动列表
- `GET /api/activities/{id}` - 获取活动详情
- `POST /api/activities/{id}/join` - 报名活动
- `DELETE /api/activities/{id}/join` - 取消报名

### 攻略接口
- `POST /api/strategies` - 创建攻略
- `GET /api/strategies` - 获取攻略列表
- `GET /api/strategies/{id}` - 获取攻略详情
- `POST /api/strategies/{id}/like` - 点赞攻略
- `DELETE /api/strategies/{id}/like` - 取消点赞

## 项目结构

```
src/main/java/com/huashidai/lvyouapp/
├── config/                 # 配置类
│   ├── MinioConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/            # 控制器
│   ├── AuthController.java
│   ├── PostController.java
│   ├── ActivityController.java
│   └── StrategyController.java
├── dto/                   # 数据传输对象
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── UserResponse.java
│   ├── PostCreateRequest.java
│   ├── PostResponse.java
│   ├── ActivityCreateRequest.java
│   ├── ActivityResponse.java
│   ├── StrategyCreateRequest.java
│   └── StrategyResponse.java
├── entity/                # 实体类
│   ├── User.java
│   ├── Post.java
│   ├── Activity.java
│   ├── Strategy.java
│   └── ...
├── repository/            # 数据访问层
│   ├── UserRepository.java
│   ├── PostRepository.java
│   ├── ActivityRepository.java
│   └── StrategyRepository.java
└── service/              # 服务层
    ├── AuthService.java
    ├── PostService.java
    ├── ActivityService.java
    ├── StrategyService.java
    ├── MinioService.java
    └── RedisService.java
```

## 开发说明

### 1. 数据库配置
请根据您的实际环境修改 `application.properties` 中的数据库连接信息。

### 2. 文件存储
项目使用MinIO作为文件存储服务，请确保MinIO服务正常运行，并创建对应的存储桶。

### 3. 缓存配置
Redis用于缓存用户会话、热点数据等，请确保Redis服务正常运行。

### 4. 安全配置
项目使用JWT进行身份认证，请妥善保管JWT密钥。

## 注意事项

1. 请确保所有外部服务（MySQL、Redis、MinIO）正常运行
2. 首次运行前请执行数据库初始化脚本
3. 生产环境请修改默认密码和密钥
4. 建议使用HTTPS协议保证数据传输安全

## 联系方式

如有问题，请联系开发团队。
