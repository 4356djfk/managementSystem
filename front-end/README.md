# Frontend

## 1. 技术栈

前端基于：

1. Vue 3
2. Vite
3. Vue Router
4. Pinia
5. Element Plus

Node 版本要求见 [package.json](C:/Users/Lenovo/Desktop/managerSystem/front-end/package.json:1)：

1. `^20.19.0`
2. `>=22.12.0`

## 2. 本地开发

```bash
npm install
npm run dev
```

默认通过 `VITE_API_BASE_URL` 指向后端，未配置时使用：

```text
http://localhost:8080
```

## 3. 生产构建

```bash
npm run build
```

生产环境容器部署已提供：

1. [Dockerfile](C:/Users/Lenovo/Desktop/managerSystem/front-end/Dockerfile:1)
2. [nginx.conf](C:/Users/Lenovo/Desktop/managerSystem/front-end/nginx.conf:1)

## 4. 页面结构

### 4.1 认证页

1. `/login`
2. `/register`

### 4.2 仪表盘工作区

挂载于 `/dashboard`，包含：

1. 首页总览
2. 模板中心
3. 用户管理
4. 我的日历
5. 全局搜索
6. 审计日志
7. 通知中心
8. 个人资料

### 4.3 项目编辑器

1. `/editor/:projectId`

该页面是项目业务主工作区，整合：

1. 项目基础信息
2. WBS / 需求 / 任务 / 里程碑
3. 甘特图与排程
4. 风险、成本、质量、测试
5. 变更、配置、沟通、采购
6. 工时、报表、归档、验收

## 5. 权限模型

前端路由根据登录用户的角色码控制可见性：

1. `SYS_ADMIN`
2. `USER`

典型限制：

1. `templates` 仅 `SYS_ADMIN`
2. `users` 仅 `SYS_ADMIN`
3. `audit` 仅 `SYS_ADMIN`
4. `calendar` 与 `search` 主要面向 `USER`
5. `project-editor` 需要认证且要求 `USER`

## 6. 与后端的集成方式

统一 HTTP 封装位于：

[src/api/http.js](C:/Users/Lenovo/Desktop/managerSystem/front-end/src/api/http.js:1)

特性：

1. 自动带 token
2. 自动解析统一返回结构
3. 遇到 `401` 自动清理本地登录态并跳回登录页

## 7. 最近前端能力更新

项目编辑器已支持并保存以下偏好：

1. 甘特配色方案
2. WBS 配置
3. 排程选项

同时任务层面已适配：

1. 截止日期
2. 计划约束类型
3. 计划约束日期
4. 依赖滞后天数

## 8. 建议的联调方式

根目录启动整套环境：

```bash
docker compose up -d --build
```

前端通过 `/api` 反向代理访问后端。
