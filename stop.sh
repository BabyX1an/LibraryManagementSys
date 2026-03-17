#!/bin/bash

echo "================================"
echo "正在停止所有服务..."
echo "================================"

# 停止前端服务
echo "停止前端服务 (Vite)..."
pkill -f "vite"
if [ $? -eq 0 ]; then
    echo "✓ 前端服务已停止"
else
    echo "✗ 前端服务未运行或停止失败"
fi

# 停止 Gateway 服务
echo "停止 Gateway 服务..."
pkill -f "spring-boot:run.*gateway"
if [ $? -eq 0 ]; then
    echo "✓ Gateway 服务已停止"
else
    echo "✗ Gateway 服务未运行或停止失败"
fi

# 停止 Book Service
echo "停止 Book Service..."
pkill -f "spring-boot:run.*book-service"
if [ $? -eq 0 ]; then
    echo "✓ Book Service 已停止"
else
    echo "✗ Book Service 未运行或停止失败"
fi

# 停止 User Service
echo "停止 User Service..."
pkill -f "spring-boot:run.*user-service"
if [ $? -eq 0 ]; then
    echo "✓ User Service 已停止"
else
    echo "✗ User Service 未运行或停止失败"
fi

# 清理 Maven 进程（备选方案）
echo "清理可能残留的 Maven 进程..."
pkill -f "spring-boot:run"

echo ""
echo "================================"
echo "所有服务停止完成！"
echo "================================"

# 显示进程状态（用于确认）
echo ""
echo "当前 Java 进程："
ps aux | grep -i "spring-boot:run\|vite" | grep -v grep || echo "无相关进程运行"
