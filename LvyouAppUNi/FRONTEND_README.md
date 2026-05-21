# 旅游社交平台前端项目

## 项目简介

基于Vue 3 + Vite + Vant的移动端旅游社交平台前端项目，支持响应式设计，适配手机端使用。

## 技术栈

- **框架**: Vue 3.5.22
- **构建工具**: Vite 7.1.7
- **UI组件库**: Vant 4.8.0
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.2.5
- **HTTP客户端**: Axios 1.6.2
- **移动端适配**: postcss-px-to-viewport

## 项目结构

```
LvyouAppUNi/
├── src/
│   ├── api/                 # API接口
│   │   ├── request.js      # Axios配置
│   │   ├── auth.js         # 认证接口
│   │   ├── posts.js        # 动态接口
│   │   ├── activities.js   # 活动接口
│   │   └── strategies.js   # 攻略接口
│   ├── components/         # 组件
│   │   ├── PostCard.vue    # 动态卡片
│   │   └── ActivityCard.vue # 活动卡片
│   ├── stores/            # 状态管理
│   │   └── user.js         # 用户状态
│   ├── router/            # 路由配置
│   │   └── index.js        # 路由定义
│   ├── views/             # 页面
│   │   ├── Home.vue        # 首页
│   │   ├── Login.vue       # 登录页
│   │   ├── Register.vue    # 注册页
│   │   ├── Posts.vue       # 动态页
│   │   ├── Activities.vue  # 活动页
│   │   ├── Profile.vue     # 个人中心
│   │   └── CreatePost.vue  # 发布动态
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── package.json            # 依赖配置
├── vite.config.js          # Vite配置
└── start-frontend.sh       # 启动脚本
```

## 核心功能

### 1. 用户认证
- 用户注册/登录
- JWT Token认证
- 自动登录状态检查
- 用户信息管理

### 2. 动态功能
- 发布动态
- 图片上传
- 位置标记
- 点赞评论
- 动态列表

### 3. 活动功能
- 活动列表
- 活动详情
- 报名参与
- 活动筛选

### 4. 个人中心
- 用户信息展示
- 个人数据统计
- 设置管理
- 退出登录

## 移动端适配

### 1. 响应式设计
- 使用Vant组件库，专为移动端设计
- 支持触摸手势操作
- 适配不同屏幕尺寸

### 2. 视口适配
- 使用postcss-px-to-viewport自动转换px为vw
- 基准宽度375px
- 支持1px边框

### 3. 性能优化
- 组件懒加载
- 图片懒加载
- 代码分割

## 快速开始

### 1. 环境要求
- Node.js 16+
- npm 8+

### 2. 安装依赖
```bash
npm install
```

### 3. 启动开发服务器
```bash
npm run dev
```

### 4. 构建生产版本
```bash
npm run build
```

## 前后端联调

### 1. 后端配置
确保后端服务运行在 `http://localhost:8080`

### 2. 前端配置
前端默认运行在 `http://localhost:5173`

### 3. API配置
在 `src/api/request.js` 中配置API基础地址：
```javascript
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})
```

### 4. 跨域配置
后端已配置CORS支持，允许前端域名访问。

## 开发指南

### 1. 添加新页面
1. 在 `src/views/` 中创建Vue组件
2. 在 `src/router/index.js` 中添加路由
3. 更新导航菜单

### 2. 添加新API
1. 在 `src/api/` 中创建对应的API文件
2. 在组件中导入并使用

### 3. 状态管理
使用Pinia进行状态管理，在 `src/stores/` 中定义store。

### 4. 组件开发
- 使用Vant组件库
- 遵循Vue 3 Composition API
- 支持TypeScript（可选）

## 部署说明

### 1. 构建项目
```bash
npm run build
```

### 2. 部署到服务器
将 `dist` 目录部署到Web服务器

### 3. Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## 注意事项

1. **移动端适配**: 项目专为移动端设计，建议在手机浏览器中测试
2. **API接口**: 确保后端API接口正常可用
3. **图片上传**: 需要配置MinIO或其他文件存储服务
4. **HTTPS**: 生产环境建议使用HTTPS协议

## 常见问题

### 1. 依赖安装失败
```bash
# 清除缓存重新安装
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### 2. 端口冲突
修改 `vite.config.js` 中的端口配置：
```javascript
server: {
  port: 5174, // 修改端口
}
```

### 3. API请求失败
检查后端服务是否正常运行，以及CORS配置是否正确。

## 联系支持

如有问题，请联系开发团队。
