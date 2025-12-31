#!/bin/bash

# 饭卡管理系统部署脚本

echo "=========================================="
echo "饭卡管理系统部署脚本"
echo "=========================================="

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装，请先安装Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 停止并删除旧容器
echo "停止并删除旧容器..."
docker-compose down

# 构建并启动服务
echo "构建并启动服务..."
docker-compose up --build -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 检查服务状态
echo "检查服务状态..."
docker-compose ps

# 显示访问信息
echo ""
echo "=========================================="
echo "部署完成！"
echo "=========================================="
echo "前端访问地址: http://localhost"
echo "后端API地址: http://localhost:8080"
echo "MySQL端口: 3306"
echo "Redis端口: 6379"
echo ""
echo "默认管理员账号: admin"
echo "默认密码: admin123"
echo ""
echo "测试用户账号: user1 / user2"
echo "测试用户密码: user123"
echo ""
echo "查看日志命令: docker-compose logs -f [服务名]"
echo "停止服务命令: docker-compose down"
echo "=========================================="