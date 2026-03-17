#!/bin/bash
echo "停止所有服务..."
pkill -f "spring-boot:run"

pkill -f "vite"
echo "服务已停止"
