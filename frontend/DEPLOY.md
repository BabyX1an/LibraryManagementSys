# 前端部署说明

## 构建产物位置

构建后的静态文件位于 `dist/` 目录下，包含：
- `index.html` - 入口 HTML 文件
- `assets/` - 所有静态资源（JS、CSS、图片等）

## Nginx 部署配置

### 1. 复制文件到 Nginx 目录

```bash
# 创建 Nginx 网站目录（如果不存在）
sudo mkdir -p /var/www/library-frontend

# 复制构建产物
sudo cp -r dist/* /var/www/library-frontend/
```

### 2. 配置 Nginx

修改本项目的 `nginx.conf` 文件，将 `root` 路径改为实际的部署路径：

```nginx
root /var/www/library-frontend;
```

然后复制到 Nginx 配置目录：

```bash
sudo cp nginx.conf /etc/nginx/sites-available/library-frontend
sudo ln -s /etc/nginx/sites-available/library-frontend /etc/nginx/sites-enabled/
```

或者直接修改 Nginx 主配置：

```bash
sudo nano /etc/nginx/nginx.conf
```

将以下配置添加到 `http` 块中：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或IP

    root /var/www/library-frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3. 重启 Nginx

```bash
# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
# 或者
sudo service nginx restart
```

### 4. 验证部署

访问 `http://your-domain.com` 或 `http://your-server-ip` 应该能看到前端页面。

## Docker 部署

### 创建 Dockerfile

```dockerfile
FROM nginx:alpine

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制静态文件
COPY dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 构建镜像

```bash
docker build -t library-frontend .
```

### 运行容器

```bash
docker run -d -p 80:80 --name library-frontend library-frontend
```

## 注意事项

1. **后端 API 地址**：确保后端 Gateway 服务运行在 `http://localhost:8081`，或修改 Nginx 配置中的 `proxy_pass` 地址

2. **生产环境建议**：
   - 使用 HTTPS
   - 设置适当的缓存策略
   - 配置 CDN 加速静态资源
   - 配置日志轮转

3. **前端路由模式**：当前配置支持 Vue Router 的 history 模式，所有路由都会指向 `index.html`

4. **文件权限**：确保 Nginx 进程对静态文件有读取权限

```bash
sudo chown -R www-data:www-data /var/www/library-frontend
sudo chmod -R 755 /var/www/library-frontend
```
