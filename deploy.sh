#!/bin/bash
# 帅气气项目部署脚本

echo "========================================="
echo "  帅气气项目 - 一键部署脚本"
echo "========================================="

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

echo "步骤 1: 构建后端服务..."
cd backend
mvn clean package -DskipTests
cd ..

echo "步骤 2: 构建并启动所有服务..."
docker-compose down
docker-compose build --no-cache
docker-compose up -d

echo "步骤 3: 等待服务启动..."
sleep 30

echo "步骤 4: 检查服务状态..."
docker-compose ps

echo "========================================="
echo "  部署完成！"
echo "========================================="
echo "前端地址: http://localhost:3000"
echo "API 网关: http://localhost:8080"
echo "Nacos 控制台: http://localhost:8848/nacos"
echo "========================================="
