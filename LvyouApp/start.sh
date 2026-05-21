#!/bin/bash

# 旅游社交平台启动脚本

echo "正在启动旅游社交平台..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误：未找到Java环境，请先安装JDK 17+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误：未找到Maven环境，请先安装Maven 3.6+"
    exit 1
fi

# 编译项目
echo "正在编译项目..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "编译失败，请检查代码"
    exit 1
fi

# 启动应用
echo "正在启动应用..."
mvn spring-boot:run

echo "应用启动完成！"
echo "访问地址：http://localhost:8080"
echo "API文档：http://localhost:8080/swagger-ui.html"
