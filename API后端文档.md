# 软件项目管理系统后端说明

## 1. 项目概述

当前后端为 `Spring Boot 3.2.4 + MyBatis-Plus + MySQL + Redis` 的单体服务，代码目录位于 `project/manageSystem`。

系统已经不是早期原型，而是覆盖以下业务域的完整后端：

1. 认证与用户
2. 项目与项目章程
3. 项目模板与演示项目初始化
4. 项目成员与权限
5. WBS、需求、范围基线
6. 任务、里程碑、任务依赖、甘特图、关键路径、进度预警
7. 风险与风险矩阵
8. 成本计划、成本基线、实际成本、EVM
9. 质量管理
10. 测试管理
11. 配置项与配置基线
12. 沟通、会议、沟通记录
13. 采购、供应商、合同
14. 工时与工时报表
15. 验收、归档、经验教训、项目报告、总结报告
16. 附件上传下载、通知、导入导出、全文搜索、审计日志
17. 项目编辑器偏好设置

## 2. 技术栈与运行方式

### 2.1 核心依赖

1. `spring-boot-starter-web`
2. `mybatis-plus-spring-boot3-starter`
3. `spring-boot-starter-data-redis`
4. `mysql-connector-j`
5. `knife4j-openapi3-jakarta-spring-boot-starter`
6. `poi-ooxml`
7. `pdfbox`

### 2.2 基础设施

1. MySQL：业务主库，默认库名 `project_management`
2. Redis：保存登录会话 token
3. Docker Compose：已支持一键编排 `mysql + redis + backend + frontend`

### 2.3 配置文件

主配置文件为 [application.yml](C:/Users/Lenovo/Desktop/managerSystem/project/manageSystem/src/main/resources/application.yml:1)，已支持通过环境变量覆盖：

1. `SPRING_DATASOURCE_URL`
2. `SPRING_DATASOURCE_USERNAME`
3. `SPRING_DATASOURCE_PASSWORD`
4. `SPRING_DATA_REDIS_HOST`
5. `SPRING_DATA_REDIS_PORT`
6. `AUTH_TOKEN_EXPIRE_MINUTES`
7. `SERVER_PORT`

## 3. 统一接口约定

### 3.1 返回结构

所有接口统一返回 `ApiResponse<T>`：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {},
  "requestId": "uuid"
}
```

分页数据统一使用 `PageResult<T>`：

```json
{
  "list": [],
  "page": 1,
  "pageSize": 20,
  "total": 0
}
```

### 3.2 认证方式

1. 登录成功后返回 token
2. 前端使用 `Authorization: Bearer <token>` 传递
3. token 会话保存在 Redis，键前缀为 `auth:token:`
4. 默认过期时间为 `10080` 分钟，可通过配置覆盖

### 3.3 权限模型

系统角色来自 `SystemRoleEnum`：

1. `SYS_ADMIN`
2. `USER`

项目内角色来自 `ProjectRoleEnum`：

1. `PROJECT_MANAGER`
2. `TEAM_MEMBER`
3. `READ_ONLY`

控制规则：

1. 系统级拦截由 `AuthInterceptor` 和 `RoleAuthorizationInterceptor` 负责
2. 写接口大多加了 `@RequireRole({SYS_ADMIN, USER})`
3. 项目级数据访问由 service 层继续校验项目成员关系

## 4. 当前后端模块

### 4.1 认证与用户

控制器：

1. `AuthController`
2. `UserController`

能力：

1. 注册
2. 登录 / 登出
3. 获取当前用户
4. 更新当前用户资料
5. 用户分页查询
6. 创建用户
7. 更新用户
8. 修改用户状态
9. 修改用户角色

### 4.2 项目主线

控制器：

1. `ProjectController`
2. `ProjectTemplateController`
3. `ProjectMemberController`
4. `ProjectViewController`
5. `ProjectEditorPreferenceController`

能力：

1. 项目分页查询、详情、新建、编辑、删除
2. 项目状态流转与关闭
3. 项目章程读取与保存
4. 项目模板查询、新增、编辑、详情
5. 从模板创建项目
6. 初始化演示项目
7. 项目成员管理与候选成员查询
8. 项目仪表盘
9. 项目日历、个人日历、关闭前检查
10. 项目编辑器偏好保存

### 4.3 范围与进度

控制器：

1. `WbsController`
2. `RequirementController`
3. `ScopeBaselineController`
4. `TaskController`
5. `MilestoneController`

能力：

1. WBS 增删改查
2. 需求分页查询、新增、编辑
3. 范围基线查询、新增、删除
4. 任务分页查询、详情、新增、编辑、删除
5. 任务进度更新
6. 任务评论
7. 任务依赖管理
8. 甘特图数据
9. 关键路径
10. 进度预警
11. 里程碑管理

### 4.4 风险、成本、质量、测试

控制器：

1. `RiskController`
2. `RiskMatrixController`
3. `CostController`
4. `QualityController`
5. `TestManagementController`

能力：

1. 风险台账、状态更新、矩阵视图
2. 成本计划、成本基线、实际成本、EVM
3. 质量计划、质量活动、质量指标
4. 测试计划、测试用例、缺陷、测试报告

### 4.5 配置、沟通、采购、结项

控制器：

1. `ConfigurationController`
2. `CommunicationController`
3. `ProcurementController`
4. `ProjectClosureController`
5. `OpsController`

能力：

1. 配置项与配置基线
2. 沟通矩阵、会议、沟通记录
3. 供应商、采购项、合同
4. 验收项、归档项、经验教训
5. 周报 / 月报 / 总结报告生成与删除
6. 项目附件上传、查询、下载、删除
7. 通知中心
8. Excel 导入、模板下载
9. 数据导出
10. 全局搜索
11. 审计日志

## 5. 最近已落地的重要变更

### 5.1 任务计划约束

`project_task` 已新增并在代码中使用以下字段：

1. `deadline_date`
2. `constraint_type`
3. `constraint_date`

对应：

1. `TaskEntity`
2. `CreateTaskDto`
3. `UpdateTaskDto`
4. `TaskListItemVO`

约束类型枚举为 `TaskConstraintTypeEnum`。

### 5.2 任务依赖滞后天数

`task_dependency` 已新增：

1. `lag_days`

对应：

1. `TaskDependencyEntity`
2. `TaskDependencyVO`
3. 任务调度与关键路径相关逻辑

### 5.3 编辑器偏好设置

已新增项目级编辑器偏好：

1. `ganttAppearance`
2. `wbsConfig`
3. `scheduleOptions`

后端接口：

1. `GET /projects/{projectId}/editor-preferences`
2. `PUT /projects/{projectId}/editor-preferences`

数据库表：`project_editor_preference`

## 6. 启动与初始化机制

### 6.1 数据库初始化

SQL 主脚本位于：

[table.sql](C:/Users/Lenovo/Desktop/managerSystem/project/manageSystem/src/main/java/com/manage/managesystem/sql/table.sql:1)

Docker 首次启动时会自动挂载执行。

### 6.2 启动时补丁

`SchemaBootstrapService` 会在启动时检查并补齐部分字段，当前主要用于：

1. `project_task` 的进度约束字段
2. `task_dependency` 的 `lag_days`

### 6.3 默认账号初始化

`AuthBootstrapService` 会初始化：

1. 系统角色
2. `admin / 123456` 管理员账号

## 7. 容器化部署

仓库根目录已提供：

1. [docker-compose.yml](C:/Users/Lenovo/Desktop/managerSystem/docker-compose.yml:1)
2. [project/manageSystem/Dockerfile](C:/Users/Lenovo/Desktop/managerSystem/project/manageSystem/Dockerfile:1)
3. [front-end/Dockerfile](C:/Users/Lenovo/Desktop/managerSystem/front-end/Dockerfile:1)
4. [.env.example](C:/Users/Lenovo/Desktop/managerSystem/.env.example:1)

启动命令：

```bash
docker compose up -d --build
```

## 8. 文档定位说明

本文件描述“当前后端实现与结构”。更细的接口清单见：

1. [API接口定稿.md](C:/Users/Lenovo/Desktop/managerSystem/API接口定稿.md:1)
2. [实体类设计.md](C:/Users/Lenovo/Desktop/managerSystem/实体类设计.md:1)
3. [数据库结构设计.md](C:/Users/Lenovo/Desktop/managerSystem/数据库结构设计.md:1)
