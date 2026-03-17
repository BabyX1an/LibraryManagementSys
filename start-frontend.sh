#!/bin/bash

echo "================================"
echo "启动前端服务..."
echo "================================"

# 创建日志目录
mkdir -p logs

# 进入前端目录
cd frontend

# 检查是否有 node_modules，如果没有则安装依赖
if [ ! -d "node_modules" ]; then
    echo "检测到首次运行，正在安装前端依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "依赖安装失败！"
        exit 1
    fi
    echo "依赖安装完成！"
fi

# 启动前端开发服务器
echo "正在启动前端开发服务器..."
nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!

# 等待前端服务启动
echo "等待前端服务启动..."
sleep 5

# 检查服务是否启动成功并获取实际端口
FRONTEND_PORT="3001"
if grep -q "Local:" ../logs/frontend.log 2>/dev/null; then
    FRONTEND_PORT=$(grep "Local:" ../logs/frontend.log | grep -oE 'http://localhost:[0-9]+' | grep -oE '[0-9]+$' | head -1)
    if [ -n "$FRONTEND_PORT" ]; then
        echo "✓ 前端服务启动成功！"
        echo ""
        echo "================================"
        echo "前端服务信息"
        echo "================================"
        echo "访问地址: http://localhost:$FRONTEND_PORT"
        echo "进程 ID: $FRONTEND_PID"
        echo ""
        echo "查看日志: tail -f logs/frontend.log"
        echo "停止服务: pkill -f vite"
        echo "================================"
        exit 0
    fi
fi

echo "前端服务可能正在启动中，请稍后检查日志"
echo "查看日志: tail -f logs/frontend.log"
