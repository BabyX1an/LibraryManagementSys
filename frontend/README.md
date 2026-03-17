# 图书管理系统前端

基于 Vue 3 + TypeScript + Element Plus 构建的图书管理系统前端应用。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **样式**: SCSS

## 功能模块

### 用户端
- **首页**: 展示热门图书
- **图书列表**: 浏览、搜索、筛选图书
- **图书详情**: 查看图书详细信息
- **用户登录**: 用户身份认证

### 管理端
- **数据统计**: 展示系统关键指标
- **图书管理**: 图书的增删改查
- **用户管理**: 用户的增删改查

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/              # API接口
│   │   ├── book.ts      # 图书相关API
│   │   └── user.ts      # 用户相关API
│   ├── assets/           # 资源文件
│   ├── components/       # 公共组件
│   │   └── Layout.vue  # 布局组件
│   ├── router/          # 路由配置
│   ├── store/           # 状态管理
│   │   └── user.ts     # 用户状态
│   ├── styles/          # 全局样式
│   ├── types/           # TypeScript类型定义
│   ├── utils/           # 工具函数
│   │   └── request.ts  # HTTP请求封装
│   ├── views/           # 页面组件
│   │   ├── Login.vue   # 登录页
│   │   ├── user/       # 用户端页面
│   │   │   ├── Home.vue
│   │   │   ├── Books.vue
│   │   │   └── BookDetail.vue
│   │   └── admin/      # 管理端页面
│   │       ├── Layout.vue
│   │       ├── Dashboard.vue
│   │       ├── Books.vue
│   │       └── Users.vue
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.node.json
└── vite.config.ts
```

## 安装依赖

```bash
npm install
```

## 开发

```bash
npm run dev
```

应用将在 http://localhost:3000 启动

## 构建

```bash
npm run build
```

## 预览生产构建

```bash
npm run preview
```

## 默认账号

- **管理员**: username: `admin`, password: `admin123`
- **普通用户**: 请在管理后台自行添加

## API配置

API代理配置在 `vite.config.ts` 中：

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8081',  // Gateway地址
      changeOrigin: true
    }
  }
}
```

如需修改后端地址，请修改上述配置。
