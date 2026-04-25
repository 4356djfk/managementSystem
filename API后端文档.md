# 软件项目管理工具 API 后端文档

## 1. 文档说明

本文档基于 `实体类设计.md` 中定义的 Entity / DTO / VO，对当前已确认 API 做后端实现级说明，重点给出：

1. 路由
2. 入参 DTO
3. 出参 VO
4. 业务规则
5. 实现建议

统一约定：

1. 接口前缀：`/api/v1`
2. 认证方式：`Bearer Token`
3. 请求体格式：`application/json`
4. 统一返回：`ApiResponse<T>`
5. 分页返回：`ApiResponse<PageResult<T>>`
6. 写接口默认记录审计日志

## 2. 通用返回模型

### 2.1 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "trace-id"
}
```

### 2.2 分页响应

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "list": [],
    "page": 1,
    "pageSize": 20,
    "total": 100
  },
  "requestId": "trace-id"
}
```

## 3. 认证与用户模块

### 3.1 登录

- 路径：`POST /auth/login`
- 入参：`LoginDto`
- 出参：`ApiResponse<LoginVO>`

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

响应 data：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| token | String | JWT 或 Session Token |
| user | UserProfileVO | 当前用户资料 |

业务规则：

1. 用户状态非 `ACTIVE` 时拒绝登录
2. 登录成功后更新 `lastLoginAt`
3. 返回角色列表，前端用于菜单渲染

### 3.2 退出登录

- 路径：`POST /auth/logout`
- 入参：无
- 出参：`ApiResponse<Void>`

### 3.3 当前用户信息

- 路径：`GET /auth/me`
- 入参：无
- 出参：`ApiResponse<UserProfileVO>`

### 3.4 用户列表

- 路径：`GET /users`
- 入参：`UserQueryDto`
- 出参：`ApiResponse<PageResult<UserListItemVO>>`

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| keyword | String | 用户名/姓名模糊搜索 |
| status | String | 状态 |
| role | String | 角色 |
| page | Integer | 页码 |
| pageSize | Integer | 页大小 |

### 3.5 创建用户

- 路径：`POST /users`
- 入参：`CreateUserDto`
- 出参：`ApiResponse<UserProfileVO>`

### 3.6 更新用户

- 路径：`PUT /users/{id}`
- 入参：`UpdateUserDto`
- 出参：`ApiResponse<UserProfileVO>`

### 3.7 更新用户状态

- 路径：`PATCH /users/{id}/status`
- 入参：`UpdateUserStatusDto`
- 出参：`ApiResponse<Void>`

## 4. 项目模块

### 4.1 项目列表

- 路径：`GET /projects`
- 入参：`ProjectQueryDto`
- 出参：`ApiResponse<PageResult<ProjectListItemVO>>`

### 4.2 创建项目

- 路径：`POST /projects`
- 入参：`CreateProjectDto`
- 出参：`ApiResponse<ProjectDetailVO>`

核心入参：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | String | 是 | 项目名称 |
| description | String | 否 | 描述 |
| startDate | LocalDate | 否 | 计划开始 |
| endDate | LocalDate | 否 | 计划结束 |
| lifeCycleModel | String | 是 | 生命周期模型 |
| ownerId | Long | 否 | 项目经理 |
| templateId | Long | 否 | 模板 ID |
| plannedBudget | BigDecimal | 否 | 计划预算 |

业务规则：

1. 同组织内项目名称建议唯一
2. 默认状态为 `PLANNING`
3. 若传入模板 ID，可初始化模板任务

### 4.3 项目详情

- 路径：`GET /projects/{projectId}`
- 入参：无
- 出参：`ApiResponse<ProjectDetailVO>`

### 4.4 更新项目

- 路径：`PUT /projects/{projectId}`
- 入参：`UpdateProjectDto`
- 出参：`ApiResponse<ProjectDetailVO>`

### 4.5 项目状态流转

- 路径：`PATCH /projects/{projectId}/status`
- 入参：`ChangeProjectStatusDto`
- 出参：`ApiResponse<Void>`

业务规则：

1. `PLANNING -> IN_PROGRESS`
2. `IN_PROGRESS -> MONITORING`
3. `MONITORING -> CLOSED`
4. 项目关闭前需校验验收、归档、待处理变更和未关闭风险

### 4.6 关闭项目

- 路径：`POST /projects/{projectId}/close`
- 入参：无
- 出参：`ApiResponse<Void>`

### 4.7 删除项目

- 路径：`DELETE /projects/{projectId}`
- 入参：无
- 出参：`ApiResponse<Void>`

说明：建议逻辑删除。

## 5. 项目章程与模板模块

### 5.1 项目章程查询

- 路径：`GET /projects/{projectId}/charter`
- 出参：`ApiResponse<ProjectCharterVO>`

### 5.2 新增项目章程

- 路径：`POST /projects/{projectId}/charter`
- 入参：`SaveProjectCharterDto`
- 出参：`ApiResponse<ProjectCharterVO>`

### 5.3 更新项目章程

- 路径：`PUT /projects/{projectId}/charter`
- 入参：`SaveProjectCharterDto`
- 出参：`ApiResponse<ProjectCharterVO>`

### 5.4 项目模板列表

- 路径：`GET /project-templates`
- 出参：`ApiResponse<PageResult<ProjectTemplateVO>>`

建议增加：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| type | String | 模板类型 |
| status | String | 模板状态 |
| page | Integer | 页码 |
| pageSize | Integer | 页大小 |

### 5.5 创建项目模板

- 路径：`POST /project-templates`
- 入参：`CreateProjectTemplateDto`
- 出参：`ApiResponse<ProjectTemplateVO>`

### 5.6 模板详情

- 路径：`GET /project-templates/{id}`
- 出参：`ApiResponse<ProjectTemplateVO>`

### 5.7 更新模板

- 路径：`PUT /project-templates/{id}`
- 入参：`UpdateProjectTemplateDto`
- 出参：`ApiResponse<ProjectTemplateVO>`

### 5.8 从模板创建项目

- 路径：`POST /projects/from-template`
- 入参：`CreateProjectFromTemplateDto`
- 出参：`ApiResponse<ProjectDetailVO>`

### 5.9 初始化演示项目

- 路径：`POST /projects/init-demo`
- 入参：无
- 出参：`ApiResponse<ProjectDetailVO>`

## 6. 项目成员模块

### 6.1 成员列表

- 路径：`GET /projects/{projectId}/members`
- 出参：`ApiResponse<List<ProjectMemberVO>>`

### 6.2 添加成员

- 路径：`POST /projects/{projectId}/members`
- 入参：`AddProjectMemberDto`
- 出参：`ApiResponse<ProjectMemberVO>`

### 6.3 更新成员角色

- 路径：`PUT /projects/{projectId}/members/{memberId}`
- 入参：`UpdateProjectMemberDto`
- 出参：`ApiResponse<ProjectMemberVO>`

### 6.4 移除成员

- 路径：`DELETE /projects/{projectId}/members/{memberId}`
- 出参：`ApiResponse<Void>`

业务规则：

1. 不允许移除项目唯一项目经理
2. 移除成员后 `memberStatus` 改为 `REMOVED`

## 7. 范围管理模块

### 7.1 WBS 列表

- 路径：`GET /projects/{projectId}/wbs`
- 出参：`ApiResponse<List<WbsNodeVO>>`

建议返回树形结构。

### 7.2 创建 WBS 节点

- 路径：`POST /projects/{projectId}/wbs`
- 入参：`CreateWbsNodeDto`
- 出参：`ApiResponse<WbsNodeVO>`

### 7.3 更新 WBS 节点

- 路径：`PUT /projects/{projectId}/wbs/{id}`
- 入参：`UpdateWbsNodeDto`
- 出参：`ApiResponse<WbsNodeVO>`

### 7.4 删除 WBS 节点

- 路径：`DELETE /projects/{projectId}/wbs/{id}`
- 出参：`ApiResponse<Void>`

业务规则：

1. 存在子节点时禁止直接删除，或要求级联删除
2. 若已被任务或需求引用，应给出提示

### 7.5 需求列表

- 路径：`GET /projects/{projectId}/requirements`
- 入参：`RequirementQueryDto`
- 出参：`ApiResponse<PageResult<RequirementVO>>`

### 7.6 创建需求

- 路径：`POST /projects/{projectId}/requirements`
- 入参：`CreateRequirementDto`
- 出参：`ApiResponse<RequirementVO>`

### 7.7 更新需求

- 路径：`PUT /projects/{projectId}/requirements/{id}`
- 入参：`UpdateRequirementDto`
- 出参：`ApiResponse<RequirementVO>`

### 7.8 范围基线列表

- 路径：`GET /projects/{projectId}/scope-baselines`
- 出参：`ApiResponse<List<ScopeBaselineVO>>`

### 7.9 创建范围基线

- 路径：`POST /projects/{projectId}/scope-baselines`
- 入参：`CreateScopeBaselineDto`
- 出参：`ApiResponse<ScopeBaselineVO>`

## 8. 进度管理模块

### 8.1 任务列表

- 路径：`GET /projects/{projectId}/tasks`
- 入参：`TaskQueryDto`
- 出参：`ApiResponse<PageResult<TaskListItemVO>>`

### 8.2 创建任务

- 路径：`POST /projects/{projectId}/tasks`
- 入参：`CreateTaskDto`
- 出参：`ApiResponse<TaskDetailVO>`

### 8.3 任务详情

- 路径：`GET /projects/{projectId}/tasks/{taskId}`
- 出参：`ApiResponse<TaskDetailVO>`

### 8.4 更新任务

- 路径：`PUT /projects/{projectId}/tasks/{taskId}`
- 入参：`UpdateTaskDto`
- 出参：`ApiResponse<TaskDetailVO>`

### 8.5 删除任务

- 路径：`DELETE /projects/{projectId}/tasks/{taskId}`
- 出参：`ApiResponse<Void>`

### 8.6 更新任务进度

- 路径：`PATCH /projects/{projectId}/tasks/{taskId}/progress`
- 入参：`UpdateTaskProgressDto`
- 出参：`ApiResponse<TaskDetailVO>`

业务规则：

1. `progress` 必须在 `0-100`
2. `status = DONE` 时建议自动将 `progress` 置为 `100`
3. 非项目经理仅允许更新本人负责任务

### 8.7 里程碑接口

- `GET /projects/{projectId}/milestones`
  - 出参：`ApiResponse<List<MilestoneVO>>`
- `POST /projects/{projectId}/milestones`
  - 入参：`CreateMilestoneDto`
  - 出参：`ApiResponse<MilestoneVO>`
- `PUT /projects/{projectId}/milestones/{id}`
  - 入参：`UpdateMilestoneDto`
  - 出参：`ApiResponse<MilestoneVO>`
- `DELETE /projects/{projectId}/milestones/{id}`
  - 出参：`ApiResponse<Void>`

### 8.8 任务依赖接口

- `GET /projects/{projectId}/task-dependencies`
  - 出参：`ApiResponse<List<TaskDependencyVO>>`
- `POST /projects/{projectId}/task-dependencies`
  - 入参：`CreateTaskDependencyDto`
  - 出参：`ApiResponse<TaskDependencyVO>`
- `DELETE /projects/{projectId}/task-dependencies/{id}`
  - 出参：`ApiResponse<Void>`

业务规则：

1. 前后置任务不能相同
2. 不允许形成循环依赖

### 8.9 甘特图数据

- 路径：`GET /projects/{projectId}/gantt`
- 出参：`ApiResponse<List<GanttTaskVO>>`

建议 VO：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 任务 ID |
| parentId | Long | 父任务 ID |
| name | String | 名称 |
| start | LocalDateTime | 开始时间 |
| end | LocalDateTime | 结束时间 |
| progress | BigDecimal | 进度 |
| type | String | 任务类型 |

### 8.10 关键路径

- 路径：`GET /projects/{projectId}/critical-path`
- 出参：`ApiResponse<List<TaskListItemVO>>`

### 8.11 进度预警

- 路径：`GET /projects/{projectId}/schedule-alerts`
- 出参：`ApiResponse<List<ScheduleAlertVO>>`

## 9. 成本管理模块

### 9.1 成本计划列表

- 路径：`GET /projects/{projectId}/cost-plans`
- 出参：`ApiResponse<List<CostPlanVO>>`

### 9.2 创建成本计划

- 路径：`POST /projects/{projectId}/cost-plans`
- 入参：`CreateCostPlanDto`
- 出参：`ApiResponse<CostPlanVO>`

### 9.3 更新成本计划

- 路径：`PUT /projects/{projectId}/cost-plans/{id}`
- 入参：`UpdateCostPlanDto`
- 出参：`ApiResponse<CostPlanVO>`

### 9.4 发布成本基线

- 路径：`POST /projects/{projectId}/cost-baselines`
- 入参：`CreateCostBaselineDto`
- 出参：`ApiResponse<CostBaselineVO>`

### 9.5 实际成本列表

- 路径：`GET /projects/{projectId}/cost-actuals`
- 出参：`ApiResponse<List<CostActualVO>>`

### 9.6 录入实际成本

- 路径：`POST /projects/{projectId}/cost-actuals`
- 入参：`CreateCostActualDto`
- 出参：`ApiResponse<CostActualVO>`

### 9.7 EVM 指标

- 路径：`GET /projects/{projectId}/evm`
- 出参：`ApiResponse<EvmMetricVO>`

## 10. 变更管理模块

### 10.1 变更请求列表

- 路径：`GET /projects/{projectId}/change-requests`
- 入参：`ChangeRequestQueryDto`
- 出参：`ApiResponse<PageResult<ChangeRequestVO>>`

查询字段建议：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| status | String | 状态 |
| priority | String | 优先级 |
| proposerId | Long | 提出人 |
| page | Integer | 页码 |
| pageSize | Integer | 页大小 |

### 10.2 提交变更请求

- 路径：`POST /projects/{projectId}/change-requests`
- 入参：`CreateChangeRequestDto`
- 出参：`ApiResponse<ChangeRequestVO>`

### 10.3 变更详情

- 路径：`GET /projects/{projectId}/change-requests/{id}`
- 出参：`ApiResponse<ChangeRequestVO>`

### 10.4 审批变更请求

- 路径：`POST /projects/{projectId}/change-requests/{id}/approve`
- 入参：`ApproveChangeRequestDto`
- 出参：`ApiResponse<ChangeRequestVO>`

业务规则：

1. 只有项目经理可审批
2. 审批时写入 `change_request_log`
3. 审批通过后可触发基线变更

### 10.5 变更流转记录

- 路径：`GET /projects/{projectId}/change-requests/{id}/logs`
- 出参：`ApiResponse<List<ChangeRequestLogVO>>`

## 11. 风险管理模块

### 11.1 风险列表

- 路径：`GET /projects/{projectId}/risks`
- 入参：`RiskQueryDto`
- 出参：`ApiResponse<PageResult<RiskVO>>`

### 11.2 创建风险

- 路径：`POST /projects/{projectId}/risks`
- 入参：`CreateRiskDto`
- 出参：`ApiResponse<RiskVO>`

### 11.3 更新风险

- 路径：`PUT /projects/{projectId}/risks/{id}`
- 入参：`UpdateRiskDto`
- 出参：`ApiResponse<RiskVO>`

### 11.4 更新风险状态

- 路径：`PATCH /projects/{projectId}/risks/{id}/status`
- 入参：`UpdateRiskStatusDto`
- 出参：`ApiResponse<RiskVO>`

### 11.5 风险矩阵数据

- 路径：`GET /projects/{projectId}/risk-matrix`
- 出参：`ApiResponse<RiskMatrixVO>`

建议响应结构：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| levels | List<RiskMatrixPointVO> | 风险矩阵点 |
| highCount | Integer | 高风险数量 |
| criticalCount | Integer | 严重风险数量 |

## 12. 工时管理模块

### 12.1 工时列表

- 路径：`GET /projects/{projectId}/timesheets`
- 入参：`TimesheetQueryDto`
- 出参：`ApiResponse<PageResult<TimesheetVO>>`

### 12.2 填报工时

- 路径：`POST /projects/{projectId}/timesheets`
- 入参：`CreateTimesheetDto`
- 出参：`ApiResponse<TimesheetVO>`

### 12.3 更新工时

- 路径：`PUT /projects/{projectId}/timesheets/{id}`
- 入参：`UpdateTimesheetDto`
- 出参：`ApiResponse<TimesheetVO>`

### 12.4 删除工时

- 路径：`DELETE /projects/{projectId}/timesheets/{id}`
- 出参：`ApiResponse<Void>`

### 12.5 工时报表

- 路径：`GET /projects/{projectId}/timesheet-reports`
- 出参：`ApiResponse<TimesheetReportVO>`

## 13. 仪表盘与报表模块

### 13.1 首页仪表盘

- 路径：`GET /dashboard/home`
- 出参：`ApiResponse<DashboardHomeVO>`

### 13.2 项目仪表盘

- 路径：`GET /projects/{projectId}/dashboard`
- 出参：`ApiResponse<ProjectDashboardVO>`

建议字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| projectCount | Integer | 项目总数，可选 |
| taskTotal | Integer | 任务总数 |
| taskCompleted | Integer | 已完成任务数 |
| taskCompletionRate | BigDecimal | 完成率 |
| plannedCost | BigDecimal | 计划成本 |
| actualCost | BigDecimal | 实际成本 |
| openRiskCount | Integer | 未关闭风险数 |
| pendingChangeCount | Integer | 待审批变更数 |
| upcomingMilestones | List<MilestoneVO> | 即将到来的里程碑 |

### 13.3 项目日历

- 路径：`GET /projects/{projectId}/calendar`
- 出参：`ApiResponse<List<CalendarEventVO>>`

### 13.4 个人日历

- 路径：`GET /calendar/my`
- 出参：`ApiResponse<List<CalendarEventVO>>`

### 13.5 生成周报/月报

- 路径：`POST /projects/{projectId}/reports/generate`
- 入参：`GenerateReportDto`
- 出参：`ApiResponse<ReportVO>`

### 13.6 报表列表

- 路径：`GET /projects/{projectId}/reports`
- 出参：`ApiResponse<PageResult<ReportVO>>`

### 13.7 项目总结报告

- 路径：`POST /projects/{projectId}/summary-report/generate`
- 入参：`GenerateReportDto`
- 出参：`ApiResponse<ReportVO>`

## 14. 附件与通知模块

### 14.1 上传附件

- 路径：`POST /attachments/upload`
- 请求类型：`multipart/form-data`
- 出参：`ApiResponse<AttachmentVO>`

建议表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | MultipartFile | 是 | 文件 |
| bizType | String | 否 | 业务类型 |
| bizId | Long | 否 | 业务 ID |

### 14.2 附件详情

- 路径：`GET /attachments/{id}`
- 出参：`ApiResponse<AttachmentVO>`

### 14.3 删除附件

- 路径：`DELETE /attachments/{id}`
- 出参：`ApiResponse<Void>`

### 14.4 通知列表

- 路径：`GET /notifications`
- 出参：`ApiResponse<PageResult<NotificationVO>>`

### 14.5 标记已读

- 路径：`PATCH /notifications/{id}/read`
- 出参：`ApiResponse<Void>`

### 14.6 全部已读

- 路径：`PATCH /notifications/read-all`
- 出参：`ApiResponse<Void>`

## 15. 收尾与导出模块

### 15.1 验收项

- `GET /projects/{projectId}/acceptance-items`
  - 出参：`ApiResponse<List<AcceptanceItemVO>>`
- `POST /projects/{projectId}/acceptance-items`
  - 入参：`CreateAcceptanceItemDto`
  - 出参：`ApiResponse<AcceptanceItemVO>`
- `PUT /projects/{projectId}/acceptance-items/{id}`
  - 入参：`UpdateAcceptanceItemDto`
  - 出参：`ApiResponse<AcceptanceItemVO>`

### 15.2 文档归档

- `GET /projects/{projectId}/archives`
  - 出参：`ApiResponse<List<ArchiveItemVO>>`
- `POST /projects/{projectId}/archives`
  - 入参：`CreateArchiveItemDto`
  - 出参：`ApiResponse<ArchiveItemVO>`

### 15.3 经验教训

- `GET /projects/{projectId}/lessons-learned`
  - 出参：`ApiResponse<List<LessonLearnedVO>>`
- `POST /projects/{projectId}/lessons-learned`
  - 入参：`CreateLessonLearnedDto`
  - 出参：`ApiResponse<LessonLearnedVO>`
- `PUT /projects/{projectId}/lessons-learned/{id}`
  - 入参：`UpdateLessonLearnedDto`
  - 出参：`ApiResponse<LessonLearnedVO>`

### 15.4 关闭前检查

- 路径：`GET /projects/{projectId}/closure-check`
- 出参：`ApiResponse<ClosureCheckVO>`

建议字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| acceptanceCompleted | Boolean | 验收是否完成 |
| archiveCompleted | Boolean | 归档是否完成 |
| openRisks | Integer | 未关闭风险数 |
| pendingChanges | Integer | 待处理变更数 |
| requiredReportsReady | Boolean | 必需报告是否齐备 |

### 15.5 数据导出

- 路径：`POST /exports`
- 入参：`ExportDataDto`
- 出参：`ApiResponse<ExportTaskVO>`

业务规则：

1. 建议异步导出
2. 返回导出任务 ID
3. 导出完成后通过通知中心提醒

### 15.6 Excel 导入

- 路径：`POST /imports/excel`
- 请求类型：`multipart/form-data`
- 出参：`ApiResponse<ImportResultVO>`

建议字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | MultipartFile | 是 | Excel 文件 |
| module | String | 是 | `WBS` / `TASK` / `RISK` |
| projectId | Long | 是 | 项目 ID |

### 15.7 全局搜索

- 路径：`GET /search`
- 入参：`SearchQueryDto`
- 出参：`ApiResponse<PageResult<SearchResultVO>>`

### 15.8 操作日志

- 路径：`GET /audit-logs`
- 入参：`AuditLogQueryDto`
- 出参：`ApiResponse<PageResult<AuditLogVO>>`

## 16. 推荐 Controller 切分

```text
AuthController
UserController
ProjectController
ProjectCharterController
ProjectTemplateController
ProjectMemberController
WbsController
RequirementController
ScopeBaselineController
TaskController
MilestoneController
TaskDependencyController
CostPlanController
CostActualController
RiskController
ChangeRequestController
TimesheetController
DashboardController
ReportController
AttachmentController
NotificationController
ClosureController
ExportController
SearchController
AuditLogController
```

## 17. 推荐 Service 切分

```text
AuthService
UserService
ProjectService
ProjectTemplateService
ProjectMemberService
WbsService
RequirementService
TaskService
MilestoneService
RiskService
ChangeRequestService
CostService
TimesheetService
DashboardService
ReportService
AttachmentService
NotificationService
ExportService
AuditLogService
```

## 18. 实现建议

1. 先实现 V1.0 主链路：认证、项目、成员、WBS、任务、风险、变更、工时、仪表盘
2. DTO 做参数校验，建议使用 `jakarta.validation`
3. VO 由 Service 层组装，避免 Controller 直接拼装
4. 所有项目级接口统一校验项目成员权限
5. 枚举值与数据库存储值保持一致，前端名称在字典层转换
6. 审批、状态流转、删除、导出必须记录审计日志

