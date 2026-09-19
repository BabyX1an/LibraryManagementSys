# 启动说明

本文档说明如何在本地启动图书管理系统。`start-all.sh` 只启动项目自身的
5 个服务；MySQL、Redis 和 Nacos 需要提前启动。

## 1. 环境要求

| 依赖 | 建议版本 | 默认端口 |
| --- | --- | ---: |
| JDK | 21 | - |
| Node.js | 18+ | - |
| Python | 3.10+ | - |
| MySQL | 8.0+ | 3306 |
| Redis | 6.0+ | 6379 |
| Nacos | 2.x | 8848 |

项目已包含 Maven Wrapper，不必单独安装 Maven。

## 2. 配置环境变量

在项目根目录执行：

```bash
cp .env.example .env
```

编辑 `.env`，至少修改以下内容：

- MySQL 用户名和密码
- Nacos namespace ID
- 图书封面存储的绝对路径

如果路径或密码包含空格、`&` 等 Shell 特殊字符，需要在 `.env` 中用引号
包住整个值。

将变量导入当前终端：

```bash
set -a
source .env
set +a
```

`.env` 已加入 `.gitignore`，不要将真实密码提交到 Git。

## 3. 启动基础设施

### MySQL

确保 MySQL 已经监听 `3306` 端口，并且 `.env` 中的账号具有创建数据库和表
的权限。

### Redis

确保 Redis 已经监听 `6379` 端口。AI 服务默认使用 Redis DB 1 存储图书向量。

### Nacos

以 standalone 模式启动 Nacos：

```bash
sh /path/to/nacos/bin/startup.sh -m standalone
```

访问 [http://localhost:8848/nacos](http://localhost:8848/nacos)，创建一个 namespace，
再将 namespace ID 填入 `.env` 中所有对应项。默认 Nacos 账号和密码通常为
`nacos` / `nacos`。

## 4. 初始化数据库

两个 SQL 文件都必须导入：

```bash
mysql -u root -p < database/db_init.sql
mysql -u root -p library_system < database/borrow_record.sql
```

`db_init.sql` 会创建 `library_system` 数据库、`book` 和 `user` 表，
`borrow_record.sql` 会创建借阅记录表。

本地测试数据包含以下账号：

| 用户名 | 角色 | 初始密码 |
| --- | --- | --- |
| `admin` | 管理员 | `admin123` |
| `teacher` | 普通用户 | `admin123` |
| `student` | 普通用户 | `admin123` |

这些账号只适用于本地演示，不要在公网环境中使用默认密码。

## 5. 准备 AI 模型

首次运行需要安装 Python 依赖：

```bash
cd ai-service
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

AI 服务当前以离线模式加载
`paraphrase-multilingual-MiniLM-L12-v2`。如果本机没有缓存该模型，需先在
能访问 Hugging Face 的网络环境中执行一次：

```bash
python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')"
```

完成后返回项目根目录：

```bash
cd ..
```

## 6. 安装前端依赖

```bash
cd frontend
npm install
cd ..
```

## 7. 一键启动

确保当前终端已经导入 `.env`，然后执行：

```bash
chmod +x start-all.sh stop-all.sh
./start-all.sh
```

启动完成后的访问地址：

| 服务 | 地址 |
| --- | --- |
| 前端 | [http://localhost:3001](http://localhost:3001) |
| API Gateway | [http://localhost:8081](http://localhost:8081) |
| Book Service | [http://localhost:8105](http://localhost:8105) |
| User Service | [http://localhost:8106](http://localhost:8106) |
| AI Service | [http://localhost:8108](http://localhost:8108) |
| Nacos | [http://localhost:8848/nacos](http://localhost:8848/nacos) |

## 8. 验证服务

```bash
# Gateway
curl http://localhost:8081/actuator/health

# Book Service
curl http://localhost:8105/test

# AI Service
curl http://localhost:8108/health

# 通过 Gateway 查询图书
curl "http://localhost:8081/api/v1/books?current=1&size=5"

# 语义搜索
curl --get "http://localhost:8081/api/v1/ai/search" \
  --data-urlencode "q=Java programming"
```

## 9. 停止服务

```bash
./stop-all.sh
```

该脚本只停止项目自身的 Gateway、Book Service、User Service、AI Service 和前端，
不会停止 MySQL、Redis 或 Nacos。

## 10. 手动启动

需要单独调试服务时，在不同终端中分别执行：

```bash
# Gateway
(cd library-gateway && ../mvnw spring-boot:run)

# Book Service
(cd book-service && ../mvnw spring-boot:run)

# User Service
(cd user-service && ../mvnw spring-boot:run)

# AI Service
(cd ai-service && venv/bin/python -m app.main)

# Frontend
(cd frontend && npm run dev)
```

每个终端都需要先执行 `set -a; source .env; set +a`。

## 11. 日志与排错

所有一键启动的日志位于 `logs/`：

```bash
tail -f logs/gateway.log
tail -f logs/book-service.log
tail -f logs/user-service.log
tail -f logs/ai-service.log
tail -f logs/frontend.log
```

常见问题：

- **Gateway 返回 503**：检查目标服务是否已注册到同一个 Nacos namespace。
- **AI 服务启动失败**：检查 Python 依赖、模型缓存、MySQL 和 Redis。
- **借阅接口报表不存在**：确认已导入 `database/borrow_record.sql`。
- **图书封面无法访问**：检查 `APP_IMAGE_LOCATION` 是否为存在且可读的绝对路径。
- **端口被占用**：用 `lsof -i :<port>` 定位占用进程。
- **前端生产构建失败**：当前存在待处理的 TypeScript 类型问题；本地启动请使用
  `npm run dev`。

## 安全提醒

当前项目使用 Base64 编码的用户 ID 作为演示 token，并使用 MD5 存储密码哈希。
这套机制仅适合本地或毕设演示，不应直接部署到公网。
