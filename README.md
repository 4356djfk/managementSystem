# Manager System

软件项目管理系统。当前仓库是一个前后端分离、支持 Docker Compose 一键部署的完整项目管理平台。

## 项目简介

系统面向课程项目、毕业设计、小中型软件项目和团队协作场景，覆盖从立项、规划、执行、监控到结项的主要管理流程。

当前技术栈：

1. 前端：`Vue 3 + Vite + Pinia + Element Plus`
2. 后端：`Spring Boot 3 + MyBatis-Plus`
3. 数据库：`MySQL 8`
4. 缓存 / 会话：`Redis 7`
5. 部署方式：`Docker Compose`

## 功能概览

当前已经实现的核心模块：

1. 认证与用户管理
2. 项目、项目章程、项目模板
3. 项目成员与权限
4. WBS、需求、范围基线
5. 任务、里程碑、任务依赖、甘特图、关键路径、进度预警
6. 风险、风险矩阵
7. 成本计划、成本基线、实际成本、EVM
8. 质量管理、测试管理
9. 配置管理、变更管理
10. 沟通管理、采购与合同
11. 工时、通知、全局搜索、审计日志
12. 验收、归档、经验教训、项目报告
13. 附件上传下载、Excel 导入导出
14. 项目编辑器偏好设置

## 项目结构

```text
managerSystem
├─ front-end                 # Vue 前端
├─ project/manageSystem      # Spring Boot 后端
├─ docker-compose.yml        # 一键部署编排
├─ .env.example              # 部署环境变量示例
├─ README.md                 # 仓库入口说明
├─ API后端文档.md
├─ API接口定稿.md
├─ 功能点定稿.md
├─ 实体类设计.md
└─ 数据库结构设计.md
```

## 快速开始

### 方式一：Docker 一键启动

仓库根目录已经提供完整编排：

[docker-compose.yml](C:/Users/Lenovo/Desktop/managerSystem/docker-compose.yml:1)

直接执行：

```bash
docker compose up -d --build
```

默认会启动以下服务：

1. `mysql`
2. `redis`
3. `backend`
4. `frontend`

默认端口：

1. 前端：`80`
2. 后端：`8080`
3. MySQL：`3306`
4. Redis：`6379`

环境变量示例见：

[.env.example](C:/Users/Lenovo/Desktop/managerSystem/.env.example:1)

### 方式二：本地分别启动

#### 启动后端

目录：

[project/manageSystem](C:/Users/Lenovo/Desktop/managerSystem/project/manageSystem:1)

依赖：

1. Java 17
2. Maven
3. MySQL
4. Redis

配置文件：

[application.yml](C:/Users/Lenovo/Desktop/managerSystem/project/manageSystem/src/main/resources/application.yml:1)

启动命令：

```bash
cd project/manageSystem
mvn spring-boot:run
```

#### 启动前端

目录：

[front-end](C:/Users/Lenovo/Desktop/managerSystem/front-end:1)

依赖：

1. Node.js `^20.19.0` 或 `>=22.12.0`

启动命令：

```bash
cd front-end
npm install
npm run dev
```

前端默认访问：

```text
http://localhost:8080
```

## 快速体验流程

如果你是第一次接触这个项目，建议按下面顺序体验：

1. 执行 `docker compose up -d --build`
2. 打开浏览器访问 `http://localhost`
3. 使用默认管理员账号登录
4. 先进入用户管理或模板中心确认系统级能力
5. 初始化一个演示项目或从模板创建项目
6. 进入项目编辑器查看 WBS、任务、甘特图、风险、成本、测试等模块

## 默认账号

后端启动时会自动执行 `AuthBootstrapService`，默认初始化管理员账号：

1. 用户名：`admin`
2. 密码：`123456`

对应代码见：

[AuthBootstrapService.java](C:/Users/Lenovo/Desktop/managerSystem/project/manageSystem/src/main/java/com/manage/managesystem/service/AuthBootstrapService.java:1)

说明：

1. 如果库里已经存在 `admin`，启动时会重置该账号密码为 `123456`
2. 生产环境不应保留该默认密码

## 数据初始化

MySQL 初始化脚本位于：

[table.sql](C:/Users/Lenovo/Desktop/managerSystem/project/manageSystem/src/main/java/com/manage/managesystem/sql/table.sql:1)

首次使用 Docker 启动时会自动执行。

后端启动时还会通过 `SchemaBootstrapService` 自动补齐部分历史字段，例如：

1. 任务截止日期
2. 任务计划约束
3. 依赖滞后天数

## 前端页面说明

### 仪表盘工作区

挂载于 `/dashboard`，包含：

1. 首页总览
2. 模板中心
3. 用户管理
4. 我的日历
5. 全局搜索
6. 审计日志
7. 通知中心
8. 个人资料

### 项目编辑器

页面路径：

1. `/editor/:projectId`

这是业务主工作区，整合：

1. 项目基础信息
2. WBS / 需求 / 范围基线
3. 任务 / 里程碑 / 依赖 / 甘特图
4. 风险 / 成本 / 质量 / 测试
5. 配置 / 变更 / 沟通 / 采购
6. 工时 / 报表 / 验收 / 归档 / 经验教训

## 后端接口说明

接口文档见：

1. [API后端文档.md](C:/Users/Lenovo/Desktop/managerSystem/API后端文档.md:1)
2. [API接口定稿.md](C:/Users/Lenovo/Desktop/managerSystem/API接口定稿.md:1)

当前接口风格说明：

1. 未使用旧版 `/api/v1` 前缀
2. 统一返回 `ApiResponse<T>`
3. 分页返回 `PageResult<T>`
4. 使用 `Bearer Token` 鉴权

## 主要设计文档

1. [功能点定稿.md](C:/Users/Lenovo/Desktop/managerSystem/功能点定稿.md:1)
2. [实体类设计.md](C:/Users/Lenovo/Desktop/managerSystem/实体类设计.md:1)
3. [数据库结构设计.md](C:/Users/Lenovo/Desktop/managerSystem/数据库结构设计.md:1)
4. [front-end/README.md](C:/Users/Lenovo/Desktop/managerSystem/front-end/README.md:1)

## 常见问题

### 1. 前端打开后无法请求后端

检查：

1. 后端是否已经启动
2. `VITE_API_BASE_URL` 是否正确
3. Docker 部署时前端是否通过 `/api` 代理访问后端

### 2. 登录失败或接口 401

检查：

1. Redis 是否正常启动
2. 浏览器本地 token 是否过期
3. 是否误改了默认管理员密码

### 3. Docker 首次启动后没有初始化表

检查：

1. 是否是首次创建 `mysql_data` 卷
2. 如果不是首次，初始化脚本不会重复执行
3. 可以删除对应数据卷后重新启动，或手动执行 SQL

### 4. 修改了数据库结构但旧库没同步

当前项目存在历史结构补丁逻辑，但只覆盖部分字段。对于大范围结构变更，仍建议：

1. 手动执行 SQL 迁移
2. 或重建本地测试库


