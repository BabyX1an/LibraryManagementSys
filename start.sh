#!/bin/bash

echo "启动微服务框架..."

# 创建日志目录
mkdir -p logs

# 检查端口是否被占用
check_port() {
    port=$1
    if lsof -i :$port >/dev/null 2>&1; then
        echo "端口 $port 已被占用"
        return 1
    fi
    return 0
}

# 启动 gateway
echo "启动 gateway 服务..."
cd library-gateway
# 直接使用默认配置（application.yml），端口为 8081
nohup mvn spring-boot:run > ../logs/gateway.log 2>&1 &
GATEWAY_PID=$!
sleep 5

# 启动 book-service
echo "启动 book-service 服务..."
cd ../book-service
nohup mvn spring-boot:run > ../logs/book-service.log 2>&1 &
BOOK_PID=$!
sleep 5

# 启动 user-service
echo "启动 user-service 服务..."
cd ../user-service
nohup mvn spring-boot:run > ../logs/user-service.log 2>&1 &
USER_PID=$!
sleep 5

# 启动 frontend
echo "启动前端服务..."
cd ../frontend

# 检查是否有 node_modules，如果没有则安装依赖
if [ ! -d "node_modules" ]; then
    echo "检测到首次运行，正在安装前端依赖..."
    npm install
fi

# 启动前端开发服务器
nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!

# 等待前端服务启动并检查端口
echo "等待前端服务启动..."
sleep 5

# 检查日志确认服务是否启动成功并获取实际端口
FRONTEND_URL="http://localhost:5173"
if grep -q "Local:" ../logs/frontend.log 2>/dev/null; then
    # 提取端口号：从日志中提取 http://localhost:xxxx/ 格式中的端口号
    FRONTEND_PORT=$(grep "Local:" ../logs/frontend.log | grep -oE 'http://localhost:[0-9]+' | grep -oE '[0-9]+$' | head -1)
    FRONTEND_URL="http://localhost:$FRONTEND_PORT"
    echo "前端服务启动成功！"
else
    echo "前端服务可能正在启动中，请稍后检查日志"
fi

echo "服务启动完成！"
echo "Gateway: http://localhost:8081"
echo "Book Service: http://localhost:8107"
echo "User Service: http://localhost:8108"
echo "Frontend: $FRONTEND_URL"
echo ""
echo "Nacos Dashboard: http://localhost:8080/index.html"
echo ""
echo "测试接口："
echo "- Gateway 健康检查: curl http://localhost:8081/actuator/health"
echo "- Book Service 测试: curl http://localhost:8107/api/test/hello"
echo "- User Service 测试: curl http://localhost:8108/api/test/hello"
echo ""
echo "查看日志："
echo "Gateway 日志: tail -f logs/gateway.log"
echo "Book Service 日志: tail -f logs/book-service.log"
echo "User Service 日志: tail -f logs/user-service.log"
echo "Frontend 日志: tail -f logs/frontend.log"

# 创建停止脚本
cat > stop.sh << 'EOF'
#!/bin/bash
echo "停止所有服务..."
pkill -f "spring-boot:run"

pkill -f "vite"
echo "服务已停止"
EOF
chmod +x stop.sh