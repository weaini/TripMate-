#!/bin/bash

# 前端项目启动脚本

echo "正在启动旅游社交平台前端项目..."

# 检查Node.js环境
if ! command -v node &> /dev/null; then
    echo "错误：未找到Node.js环境，请先安装Node.js 16+"
    exit 1
fi

# 检查npm环境
if ! command -v npm &> /dev/null; then
    echo "错误：未找到npm环境，请先安装npm"
    exit 1
fi

# 安装依赖
echo "正在安装依赖..."
npm install

if [ $? -ne 0 ]; then
    echo "依赖安装失败，请检查网络连接"
    exit 1
fi

# 启动开发服务器
echo "正在启动开发服务器..."
npm run dev

echo "前端项目启动完成！"
echo "访问地址：http://localhost:5173"
