#!/bin/bash

# ============================================================
#  图书管理系统 - 全服务一键启动脚本
#  包含: Gateway / Book Service / User Service / AI Service / Frontend
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_DIR/logs"
PID_DIR="$PROJECT_DIR/logs/pids"

# 创建必要目录
mkdir -p "$LOG_DIR" "$PID_DIR"

# ---------- 颜色 ----------
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ---------- 工具函数 ----------
print_banner() {
  echo ""
  echo -e "${CYAN}=============================================${NC}"
  echo -e "${CYAN}   图书管理系统 - 全服务启动${NC}"
  echo -e "${CYAN}=============================================${NC}"
  echo ""
}

check_port() {
  local port=$1
  if lsof -i :"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    return 1
  fi
  return 0
}

wait_for_port() {
  local port=$1
  local name=$2
  local max_wait=${3:-30}
  local waited=0
  while ! lsof -i :"$port" -sTCP:LISTEN >/dev/null 2>&1; do
    sleep 1
    waited=$((waited + 1))
    if [ "$waited" -ge "$max_wait" ]; then
      echo -e "  ${YELLOW}等待超时(${max_wait}s)，请检查日志: $LOG_DIR/${name}.log${NC}"
      return 1
    fi
  done
  return 0
}

kill_if_running() {
  local pid_file=$1
  local name=$2
  if [ -f "$pid_file" ]; then
    local pid
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
      echo -e "  ${YELLOW}$name 已在运行 (PID: $pid)，先停止...${NC}"
      kill "$pid" 2>/dev/null || true
      sleep 2
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$pid_file"
  fi
}

# ---------- 前置检查 ----------
print_banner

echo -e "${CYAN}[前置检查]${NC}"

# Java
if ! command -v java &>/dev/null; then
  echo -e "  ${RED}未找到 java，请先安装 JDK 17+${NC}"
  exit 1
fi
echo -e "  ${GREEN}Java: $(java -version 2>&1 | head -1)${NC}"

# Maven wrapper
MVN="$PROJECT_DIR/mvnw"
if [ ! -x "$MVN" ]; then
  echo -e "  ${YELLOW}mvnw 不可执行，尝试使用系统 mvn${NC}"
  MVN="mvn"
fi

# Node
if ! command -v node &>/dev/null; then
  echo -e "  ${RED}未找到 node，请先安装 Node.js${NC}"
  exit 1
fi
echo -e "  ${GREEN}Node: $(node -v)${NC}"

# Python
if ! command -v python3 &>/dev/null; then
  echo -e "  ${RED}未找到 python3，请先安装 Python 3${NC}"
  exit 1
fi
echo -e "  ${GREEN}Python: $(python3 --version)${NC}"

echo ""

# ============================================================
#  1. 启动 Gateway (8081)
# ============================================================
echo -e "${CYAN}[1/5] 启动 Gateway (端口 8081)...${NC}"
kill_if_running "$PID_DIR/gateway.pid" "Gateway"

if check_port 8081; then
  cd "$PROJECT_DIR/library-gateway"
  nohup "$MVN" spring-boot:run > "$LOG_DIR/gateway.log" 2>&1 &
  echo $! > "$PID_DIR/gateway.pid"
  if wait_for_port 8081 "gateway" 40; then
    echo -e "  ${GREEN}Gateway 启动成功${NC}"
  fi
else
  echo -e "  ${YELLOW}端口 8081 已被占用，跳过${NC}"
fi

# ============================================================
#  2. 启动 Book Service (8105)
# ============================================================
echo -e "${CYAN}[2/5] 启动 Book Service (端口 8105)...${NC}"
kill_if_running "$PID_DIR/book-service.pid" "Book Service"

if check_port 8105; then
  cd "$PROJECT_DIR/book-service"
  nohup "$MVN" spring-boot:run > "$LOG_DIR/book-service.log" 2>&1 &
  echo $! > "$PID_DIR/book-service.pid"
  if wait_for_port 8105 "book-service" 40; then
    echo -e "  ${GREEN}Book Service 启动成功${NC}"
  fi
else
  echo -e "  ${YELLOW}端口 8105 已被占用，跳过${NC}"
fi

# ============================================================
#  3. 启动 User Service (8106)
# ============================================================
echo -e "${CYAN}[3/5] 启动 User Service (端口 8106)...${NC}"
kill_if_running "$PID_DIR/user-service.pid" "User Service"

if check_port 8106; then
  cd "$PROJECT_DIR/user-service"
  nohup "$MVN" spring-boot:run > "$LOG_DIR/user-service.log" 2>&1 &
  echo $! > "$PID_DIR/user-service.pid"
  if wait_for_port 8106 "user-service" 40; then
    echo -e "  ${GREEN}User Service 启动成功${NC}"
  fi
else
  echo -e "  ${YELLOW}端口 8106 已被占用，跳过${NC}"
fi

# ============================================================
#  4. 启动 AI Service (8108)
# ============================================================
echo -e "${CYAN}[4/5] 启动 AI Service (端口 8108)...${NC}"
kill_if_running "$PID_DIR/ai-service.pid" "AI Service"

if check_port 8108; then
  cd "$PROJECT_DIR/ai-service"

  # 检查虚拟环境
  if [ -d "venv" ]; then
    PYTHON="venv/bin/python"
  else
    PYTHON="python3"
  fi

  # 检查依赖是否安装
  if ! "$PYTHON" -c "import fastapi" 2>/dev/null; then
    echo -e "  ${YELLOW}首次运行，安装 Python 依赖...${NC}"
    if [ ! -d "venv" ]; then
      python3 -m venv venv
      PYTHON="venv/bin/python"
    fi
    "$PYTHON" -m pip install -r requirements.txt -q
  fi

  nohup "$PYTHON" -m app.main > "$LOG_DIR/ai-service.log" 2>&1 &
  echo $! > "$PID_DIR/ai-service.pid"
  if wait_for_port 8108 "ai-service" 20; then
    echo -e "  ${GREEN}AI Service 启动成功${NC}"
  fi
else
  echo -e "  ${YELLOW}端口 8108 已被占用，跳过${NC}"
fi

# ============================================================
#  5. 启动 Frontend (3001)
# ============================================================
echo -e "${CYAN}[5/5] 启动 Frontend (端口 3001)...${NC}"
kill_if_running "$PID_DIR/frontend.pid" "Frontend"

if check_port 3001; then
  cd "$PROJECT_DIR/frontend"

  if [ ! -d "node_modules" ]; then
    echo -e "  ${YELLOW}首次运行，安装前端依赖...${NC}"
    npm install
  fi

  nohup npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
  echo $! > "$PID_DIR/frontend.pid"
  if wait_for_port 3001 "frontend" 15; then
    echo -e "  ${GREEN}Frontend 启动成功${NC}"
  fi
else
  echo -e "  ${YELLOW}端口 3001 已被占用，跳过${NC}"
fi

# ============================================================
#  总结
# ============================================================
echo ""
echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}   所有服务启动完成！${NC}"
echo -e "${CYAN}=============================================${NC}"
echo ""
echo -e "  访问地址:"
echo -e "    ${GREEN}Frontend:     http://localhost:3001${NC}"
echo -e "    Gateway:      http://localhost:8081"
echo -e "    Book Service: http://localhost:8105"
echo -e "    User Service: http://localhost:8106"
echo -e "    AI Service:   http://localhost:8108"
echo ""
echo -e "  查看日志:"
echo -e "    tail -f $LOG_DIR/gateway.log"
echo -e "    tail -f $LOG_DIR/book-service.log"
echo -e "    tail -f $LOG_DIR/user-service.log"
echo -e "    tail -f $LOG_DIR/ai-service.log"
echo -e "    tail -f $LOG_DIR/frontend.log"
echo ""
echo -e "  停止所有服务: ${YELLOW}./stop-all.sh${NC}"
echo ""
