# 图书馆管理系统 (Library Management System)

基于 Spring Cloud 微服务架构的智能图书推荐与管理系统，采用分布式设计，支持高并发访问。

## 📋 项目简介

本项目是一个现代化的图书馆管理系统，采用微服务架构设计，主要功能包括：

- 📚 图书信息管理
- 👤 用户管理
- 🔙 服务网关
- 📊 API 文档
- ⚡ Redis 缓存
- 💾 MySQL 数据存储

## 🏗️ 技术架构

### 核心技术栈
- **Spring Boot 3.2.0** - 主框架
- **Spring Cloud 2023.0.0** - 微服务框架
- **Spring Cloud Alibaba 2023.0.1.0** - 微服务组件
- **Nacos** - 服务注册与配置中心
- **Spring Cloud Gateway** - API 网关
- **MyBatis Plus** - ORM 框架
- **MySQL 8.0** - 数据库
- **Redis 3.2.1** - 缓存
- **Knife4j** - API 文档

### 微服务模块
| 服务名称 | 端口 | 功能描述 |
|---------|------|---------|
| library-gateway | 8081 | API 网关，路由转发 |
| book-service | 8105 | 图书管理服务 |
| user-service | 8106 | 用户管理服务 |
| simple-book-service | 8107 | 简化版图书服务 |
| simple-user-service | 8108 | 简化版用户服务 |

## 🚀 快速开始

### 环境要求

- **JDK 21** 或更高版本
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **Nacos 2.2.3+** (可选，如果不使用 Nacos，项目可运行简化版服务)

### 数据库初始化

1. 创建数据库并导入初始数据：

```bash
# 登录 MySQL
mysql -u root -p

# 执行数据库初始化脚本
source /path/to/project/LibraryManagementSys/database/db_init.sql
```

2. 确保数据库配置正确：
   - 数据库名：`library_system`
   - 用户名：`root`
   - 密码：`root`
   - 端口：`3306`

### 启动方式

#### 方式一：使用启动脚本（推荐）

```bash
# 给启动脚本添加执行权限
chmod +x start.sh

# 执行启动脚本
./start.sh
```

#### 方式二：手动启动

1. **启动 Nacos**（如果使用 Nacos 配置）：
```bash
# 下载并启动 Nacos
# 解压 nacos-server-2.2.3.tar.gz
cd nacos/bin
./startup.sh -m standalone
```

2. **启动各个微服务**：

```bash
# 启动网关服务
cd library-gateway
mvn spring-boot:run

# 启动图书服务
cd ../book-service
mvn spring-boot:run

# 启动用户服务
cd ../user-service
mvn spring-boot:run

# 启动简化版服务（可选）
cd ../simple-book-service
mvn spring-boot:run

cd ../simple-user-service
mvn spring-boot:run
```

## 🌐 访问地址

### 服务访问
| 服务 | 地址 | 说明 |
|------|------|------|
| **Gateway 网关** | http://localhost:8081 | 统一入口，端口 8081 |
| **Book Service** | http://localhost:8105 | 图书管理服务 |
| **User Service** | http://localhost:8106 | 用户管理服务 |
| **Simple Book Service** | http://localhost:8107 | 简化版图书服务 |
| **Simple User Service** | http://localhost:8108 | 简化版用户服务 |

### API 文档
| 服务 | 文档地址 |
|------|----------|
| **Book Service** | http://localhost:8105/doc.html |
| **User Service** | http://localhost:8106/doc.html |
| **Simple Book Service** | http://localhost:8107/doc.html |
| **Simple User Service** | http://localhost:8108/doc.html |

### 系统监控
- **Nacos Dashboard**: http://localhost:8848/nacos (默认用户名/密码：nacos/nacos)
- **Gateway 健康检查**: http://localhost:8081/actuator/health

### API 测试示例

```bash
# 网关测试
curl http://localhost:8081/actuator/health

# 图书服务测试
curl http://localhost:8105/api/test/hello

# 用户服务测试
curl http://localhost:8106/api/test/hello

# 简化版服务测试
curl http://localhost:8107/api/test/hello
curl http://localhost:8108/api/test/hello
```

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_system
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Redis 配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### Nacos 配置（可选）
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: adbaa99d-5656-4a9c-87fe-0c51137d338f
```

## 📁 项目结构

```
LibraryManagementSys/
├── library-gateway/          # API 网关服务
│   └── src/main/resources/
│       └── application.yml
├── book-service/            # 图书管理服务
│   └── src/main/resources/
│       └── application.yml
├── user-service/            # 用户管理服务
│   └── src/main/resources/
│       └── application.yml
├── simple-book-service/     # 简化版图书服务
├── simple-user-service/     # 简化版用户服务
├── database/                # 数据库相关文件
│   └── db_init.sql          # 数据库初始化脚本
├── start.sh                 # 启动脚本
└── stop.sh                  # 停止脚本（启动脚本自动生成）
```

## 🛑 停止服务

执行启动脚本后会自动生成 `stop.sh`，使用以下命令停止所有服务：

```bash
./stop.sh
```

或者使用：
```bash
pkill -f "spring-boot:run"
```

## 🔍 故障排除

### 常见问题

1. **端口被占用**
   - 检查端口是否被占用：`lsof -i :端口号`
   - 修改配置文件中的端口号或停止占用端口的进程

2. **数据库连接失败**
   - 确保 MySQL 服务已启动
   - 检查数据库配置是否正确
   - 确认数据库 `library_system` 已创建

3. **Redis 连接失败**
   - 确保 Redis 服务已启动
   - 检查 Redis 配置是否正确

4. **Nacos 连接失败**
   - 启动 Nacos 服务
   - 检查 Nacos 配置是否正确
   - 可以不使用 Nacos，直接使用简化版服务

### 日志查看

```bash
# 查看网关日志
tail -f logs/gateway.log

# 查看图书服务日志
tail -f logs/book-service.log

# 查看用户服务日志
tail -f logs/user-service.log
```

## 📚 API 使用示例

### 通过网关访问服务

```bash
# 访问图书相关 API
curl http://localhost:8081/api/book/books

# 访问用户相关 API
curl http://localhost:8081/api/user/users
```

### 直接访问服务

```bash
# 直接访问图书服务
curl http://localhost:8105/books

# 直接访问用户服务
curl http://localhost:8106/users
```

## 📝 开发说明

本项目采用 Maven 多模块管理，每个微服务都可以独立开发和部署。开发时建议：

1. 使用 IDE（如 IntelliJ IDEA）打开整个项目
2. 分别启动各个微服务模块
3. 通过 API 网关统一访问后端服务
4. 使用 Knife4j 查看和测试 API

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支
3. 提交变更
4. 发起 Pull Request

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 项目邮箱：admin@library.com
- 技术支持：管理员

---

*最后更新：2026年1月*