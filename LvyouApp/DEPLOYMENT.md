# 旅游社交平台部署指南

## 环境要求

### 基础环境
- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **MinIO**: 最新版本

### 系统要求
- **内存**: 最少2GB，推荐4GB+
- **磁盘**: 最少10GB可用空间
- **网络**: 稳定的网络连接

## 部署步骤

### 1. 环境准备

#### 安装JDK 17
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# 验证安装
java -version
```

#### 安装Maven
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# 验证安装
mvn -version
```

### 2. 数据库部署

#### MySQL安装和配置
```bash
# Ubuntu/Debian
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# 启动MySQL服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 创建数据库和用户
mysql -u root -p
```

```sql
-- 创建数据库
CREATE DATABASE lvyou_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'lvyou_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON lvyou_app.* TO 'lvyou_user'@'localhost';
FLUSH PRIVILEGES;

-- 执行初始化脚本
source /path/to/your/project/src/main/resources/sql/init.sql;
```

### 3. Redis部署

#### Redis安装
```bash
# Ubuntu/Debian
sudo apt install redis-server

# CentOS/RHEL
sudo yum install redis

# 启动Redis服务
sudo systemctl start redis
sudo systemctl enable redis

# 验证Redis
redis-cli ping
```

### 4. MinIO部署

#### MinIO安装
```bash
# 下载MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio

# 创建数据目录
mkdir -p /opt/minio/data

# 启动MinIO
./minio server /opt/minio/data --console-address ":9001"
```

#### MinIO配置
1. 访问管理界面：http://localhost:9001
2. 默认用户名：minioadmin
3. 默认密码：minioadmin
4. 创建存储桶：lvyou-app

### 5. 应用部署

#### 配置修改
修改 `src/main/resources/application.properties` 中的配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://your-mysql-host:3306/lvyou_app?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password

# Redis配置
spring.data.redis.host=your-redis-host
spring.data.redis.port=6379
spring.data.redis.password=your_redis_password

# MinIO配置
minio.endpoint=http://your-minio-host:9000
minio.access-key=your_access_key
minio.secret-key=your_secret_key

# 邮件配置
spring.mail.username=your-email@qq.com
spring.mail.password=your-email-password
```

#### 编译和运行
```bash
# 进入项目目录
cd LvyouApp

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/LvyouApp-0.0.1-SNAPSHOT.jar
```

### 6. 生产环境部署

#### 使用systemd管理服务
创建服务文件 `/etc/systemd/system/lvyou-app.service`：

```ini
[Unit]
Description=Lvyou App
After=network.target

[Service]
Type=simple
User=lvyou
Group=lvyou
WorkingDirectory=/opt/lvyou-app
ExecStart=/usr/bin/java -jar /opt/lvyou-app/target/LvyouApp-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### 启动服务
```bash
# 重新加载systemd配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start lvyou-app

# 设置开机自启
sudo systemctl enable lvyou-app

# 查看服务状态
sudo systemctl status lvyou-app
```

### 7. Nginx反向代理（可选）

#### 安装Nginx
```bash
# Ubuntu/Debian
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx
```

#### 配置Nginx
创建配置文件 `/etc/nginx/sites-available/lvyou-app`：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 启用配置
```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/lvyou-app /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启Nginx
sudo systemctl restart nginx
```

## 监控和维护

### 日志查看
```bash
# 查看应用日志
tail -f logs/lvyou-app.log

# 查看系统日志
journalctl -u lvyou-app -f
```

### 性能监控
- 使用 `htop` 或 `top` 监控系统资源
- 使用 `mysqladmin` 监控MySQL状态
- 使用 `redis-cli info` 监控Redis状态

### 备份策略
```bash
# 数据库备份
mysqldump -u root -p lvyou_app > backup_$(date +%Y%m%d_%H%M%S).sql

# Redis备份
redis-cli BGSAVE

# 应用备份
tar -czf lvyou-app-backup-$(date +%Y%m%d_%H%M%S).tar.gz /opt/lvyou-app/
```

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否运行
   - 验证数据库连接配置
   - 检查防火墙设置

2. **Redis连接失败**
   - 检查Redis服务是否运行
   - 验证Redis配置
   - 检查网络连接

3. **MinIO连接失败**
   - 检查MinIO服务是否运行
   - 验证访问密钥配置
   - 检查存储桶是否存在

4. **应用启动失败**
   - 检查Java版本
   - 查看应用日志
   - 验证配置文件

### 日志分析
```bash
# 查看错误日志
grep -i error logs/lvyou-app.log

# 查看警告日志
grep -i warn logs/lvyou-app.log

# 实时监控日志
tail -f logs/lvyou-app.log | grep -E "(ERROR|WARN|Exception)"
```

## 安全建议

1. **数据库安全**
   - 使用强密码
   - 限制数据库访问IP
   - 定期更新数据库

2. **应用安全**
   - 使用HTTPS
   - 定期更新依赖
   - 配置防火墙

3. **系统安全**
   - 定期更新系统
   - 使用非root用户运行应用
   - 配置日志轮转

## 联系支持

如遇到问题，请联系开发团队或查看项目文档。
