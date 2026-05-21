# 景点模块说明文档

## 功能概述

景点模块提供了完整的景点管理功能，包括：
- **管理员功能**：创建、更新、删除景点（仅管理员）
- **用户功能**：浏览景点列表、查看详情、收藏/取消收藏景点
- **现代化UI**：响应式设计，适配移动端

## 数据库表结构

### 1. attractions（景点表）
- 基本信息：名称、描述、介绍、位置信息
- 分类信息：类型、等级（5A/4A等）、状态
- 实用信息：门票价格、开放时间、联系方式、网站
- 统计数据：浏览量、收藏数、评分

### 2. attraction_images（景点图片表）
- 支持多图片展示
- 支持图片排序和说明

### 3. attraction_favorites（景点收藏表）
- 用户与景点的多对多关系
- 唯一约束：同一用户不能重复收藏同一景点

## API接口

### 公开接口（无需登录）

#### 获取景点列表
```
GET /api/attractions
参数：
- keyword: 搜索关键词
- type: 景点类型（NATURAL, CULTURAL, ENTERTAINMENT等）
- city: 城市
- province: 省份
- level: 等级（LEVEL_5A, LEVEL_4A等）
- sort: 排序（default, popular, favorite）
- page: 页码
- size: 每页数量
```

#### 获取景点详情
```
GET /api/attractions/{id}
```

### 用户接口（需要登录）

#### 收藏景点
```
POST /api/attractions/{id}/favorite
```

#### 取消收藏
```
DELETE /api/attractions/{id}/favorite
```

#### 获取我的收藏列表
```
GET /api/attractions/favorites
参数：
- page: 页码
- size: 每页数量
```

#### 检查收藏状态
```
GET /api/attractions/{id}/favorite/status
```

### 管理员接口（需要管理员权限）

#### 创建景点
```
POST /api/admin/attractions
Body: AttractionCreateRequest
```

#### 更新景点
```
PUT /api/admin/attractions/{id}
Body: AttractionUpdateRequest
```

#### 删除景点
```
DELETE /api/admin/attractions/{id}
```

#### 获取所有景点（管理用）
```
GET /api/admin/attractions
```

## 前端页面

### 1. 景点列表页（/attractions）
- 搜索功能
- 类型筛选（自然景观、文化古迹等）
- 城市筛选
- 排序功能（默认、最受欢迎、收藏最多）
- 卡片式展示，包含封面图、名称、位置、类型、等级、评分、价格等信息

### 2. 景点详情页（/attractions/:id）
- 图片轮播
- 基本信息展示
- 详细介绍
- 实用信息（开放时间、联系方式、地址等）
- 游玩提示
- 收藏功能
- 位置信息（预留地图接口）

### 3. 我的收藏页（/my-favorites）
- 收藏列表展示
- 取消收藏功能
- 跳转到景点详情

## 景点类型枚举

- NATURAL: 自然景观
- CULTURAL: 文化古迹
- ENTERTAINMENT: 娱乐休闲
- MUSEUM: 博物馆
- PARK: 公园
- TEMPLE: 寺庙
- MOUNTAIN: 山岳
- WATER: 水域
- ARCHITECTURE: 建筑
- OTHER: 其他

## 景点等级枚举

- LEVEL_5A: 5A级
- LEVEL_4A: 4A级
- LEVEL_3A: 3A级
- LEVEL_2A: 2A级
- LEVEL_1A: 1A级
- UNRATED: 未评级

## 使用说明

### 初始化数据库

执行SQL脚本创建表结构：
```sql
-- 执行 LvyouApp/src/main/resources/sql/init_attractions.sql
```

### 管理员创建景点

1. 登录管理员账号
2. 访问管理后台或直接调用API
3. 填写景点信息（必填：名称、城市、类型）
4. 上传封面图片和详情图片
5. 保存

### 用户使用

1. **浏览景点**：访问 `/attractions` 页面，可以搜索、筛选、排序
2. **查看详情**：点击景点卡片进入详情页
3. **收藏景点**：在详情页点击收藏按钮（需要登录）
4. **查看收藏**：在个人中心点击"我的收藏"

## 注意事项

1. 景点创建和修改仅限管理员
2. 收藏功能需要用户登录
3. 景点状态为DELETED时，普通用户无法查看
4. 图片上传功能需要配合文件上传服务使用
5. 地图功能预留接口，待后续集成地图服务

## 后续优化建议

1. 集成地图服务（高德地图/百度地图）显示景点位置
2. 添加景点评论功能
3. 添加景点评分和评价功能
4. 添加景点推荐算法
5. 添加景点路线规划功能
6. 添加景点周边推荐

