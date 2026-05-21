#!/bin/bash

# ============================================================
#  图书管理系统 - 全服务一键停止脚本
# ============================================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_DIR="$PROJECT_DIR/logs/pids"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo ""
echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}   停止所有服务...${NC}"
echo -e "${CYAN}=============================================${NC}"
echo ""

stop_service() {
  local name=$1
  local pid_file="$PID_DIR/$2.pid"
  local port=$3

  # 先尝试用 PID 文件停止
  if [ -f "$pid_file" ]; then
    local pid
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null
      sleep 2
      if kill -0 "$pid" 2>/dev/null; then
        kill -9 "$pid" 2>/dev/null
      fi
      echo -e "  ${GREEN}$name 已停止 (PID: $pid)${NC}"
    else
      echo -e "  ${YELLOW}$name 进程已不存在${NC}"
    fi
    rm -f "$pid_file"
  else
    # 兜底：按端口查找进程并停止
    local pids
    pids=$(lsof -ti :"$port" -sTCP:LISTEN 2>/dev/null || true)
    if [ -n "$pids" ]; then
      echo "$pids" | xargs kill 2>/dev/null || true
      sleep 1
      echo "$pids" | xargs kill -9 2>/dev/null || true
      echo -e "  ${GREEN}$name 已停止 (port: $port)${NC}"
    else
      echo -e "  ${YELLOW}$name 未运行${NC}"
    fi
  fi
}

stop_service "Frontend"     "frontend"    3001
stop_service "AI Service"   "ai-service"  8108
stop_service "User Service" "user-service" 8106
stop_service "Book Service" "book-service" 8105
stop_service "Gateway"      "gateway"      8081

echo ""
echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}   所有服务已停止${NC}"
echo -e "${CYAN}=============================================${NC}"
echo ""
