#!/bin/bash

echo "================================"
echo "停止前端服务..."
echo "================================"

# 停止前端服务 (Vite)
echo "正在停止 Vite 开发服务器..."
pkill -f "vite"

# 检查是否停止成功
sleep 1
if pgrep -f "vite" > /dev/null; then
    echo "⚠️  前端服务可能仍在运行，尝试强制停止..."
    pkill -9 -f "vite"
    sleep 1
    if pgrep -f "vite" > /dev/null; then
        echo "✗ 前端服务停止失败"
        echo ""
        echo "正在运行的 Vite 进程："
        ps aux | grep -i "vite" | grep -v grep
        exit 1
    fi
fi

echo "✓ 前端服务已成功停止"
echo ""
echo "================================"
echo "状态检查："
echo "================================"
if pgrep -f "vite" > /dev/null; then
    echo "⚠️  仍有 Vite 进程运行"
else
    echo "✓ 没有 Vite 进程运行"
fi
echo ""
echo "================================"
