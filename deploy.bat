@echo off
REM 饭卡管理系统部署脚本 - Windows版本

echo ==========================================
echo 饭卡管理系统部署脚本
echo ==========================================

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker未安装，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 检查Docker Compose是否安装
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker Compose未安装，请先安装Docker Compose
    pause
    exit /b 1
)

REM 停止并删除旧容器
echo 停止并删除旧容器...
docker-compose down

REM 构建并启动服务
echo 构建并启动服务...
docker-compose up --build -d

REM 等待服务启动
echo 等待服务启动...
timeout /t 30 /nobreak >nul

REM 检查服务状态
echo 检查服务状态...
docker-compose ps

REM 显示访问信息
echo.
echo ==========================================
echo 部署完成！
echo ==========================================
echo 前端访问地址: http://localhost
echo 后端API地址: http://localhost:8080
echo MySQL端口: 3306
echo Redis端口: 6379
echo.
echo 默认管理员账号: admin
echo 默认密码: admin123
echo.
echo 测试用户账号: user1 / user2
echo 测试用户密码: user123
echo.
echo 查看日志命令: docker-compose logs -f [服务名]
echo 停止服务命令: docker-compose down
echo ==========================================
pause