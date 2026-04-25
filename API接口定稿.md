# 软件项目管理工具 API 接口定稿

## 1. 文档目标

本文档给出软件项目管理工具的后端接口定稿，采用 REST 风格，供前后端联调、数据库设计和权限设计使用。

默认约定：

1. 接口前缀：`/api/v1`
2. 数据格式：`application/json`
3. 时间格式：`YYYY-MM-DD` 或 `YYYY-MM-DD HH:mm:ss`
4. 认证方式：`Bearer Token`
5. 统一主键：`id`
6. 统一分页参数：`page`、`pageSize`

## 2. 统一返回结构

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "trace-id"
}
```

分页返回示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "list": [],
    "page": 1,
    "pageSize": 20,
    "total": 100
  }
}
```

## 3. 权限模型

### 3.1 系统角色

1. `PROJECT_MANAGER`
2. `TEAM_MEMBER`
3. `READ_ONLY`

### 3.2 项目级权限规则

1. 项目经理可读写项目全部数据并执行审批、关闭、归档。
2. 团队成员可读项目相关数据，编辑本人任务、工时、评论、风险、变更申请。
3. 只读访客仅可读取项目信息、报表、仪表盘。

## 4. 核心数据对象

| 对象 | 说明 |
| --- | --- |
| User | 用户 |
| Project | 项目 |
| ProjectCharter | 项目章程 |
| ProjectTemplate | 项目模板 |
| ProjectMember | 项目成员 |
| WbsNode | WBS 节点 |
| Requirement | 需求 |
| ScopeBaseline | 范围基准 |
| Task | 任务 |
| Milestone | 里程碑 |
| TaskDependency | 任务依赖 |
| CostPlan | 成本计划 |
| CostActual | 实际成本 |
| QualityPlan | 质量计划 |
| TestPlan | 测试计划 |
| TestCase | 测试用例 |
| Defect | 缺陷 |
| ChangeRequest | 变更请求 |
| ConfigItem | 配置项 |
| Baseline | 基线 |
| Risk | 风险 |
| CommunicationRecord | 沟通记录 |
| Meeting | 会议 |
| Comment | 评论 |
| Attachment | 附件 |
| Supplier | 供应商 |
| ProcurementItem | 采购项 |
| Contract | 合同 |
| Timesheet | 工时 |
| Notification | 通知 |
| AcceptanceItem | 验收项 |
| ArchiveItem | 归档项 |
| LessonLearned | 经验教训 |
| Report | 报表 |
| AuditLog | 审计日志 |

## 5. 认证与用户接口

## 5.1 登录

- `POST /auth/login`

请求体：

```json
{
  "username": "pm01",
  "password": "123456"
}
```

响应体：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "name": "项目经理",
      "role": "PROJECT_MANAGER"
    }
  }
}
```

## 5.2 退出

- `POST /auth/logout`

## 5.3 当前用户信息

- `GET /auth/me`

## 5.4 用户列表

- `GET /users`

查询参数：

1. `keyword`
2. `status`
3. `role`

## 5.5 创建用户

- `POST /users`

## 5.6 更新用户

- `PUT /users/{id}`

## 5.7 禁用用户

- `PATCH /users/{id}/status`

## 6. 项目初始化接口

## 6.1 项目列表

- `GET /projects`

查询参数：

1. `keyword`
2. `status`
3. `lifeCycleModel`
4. `ownerId`

## 6.2 创建项目

- `POST /projects`

请求体示例：

```json
{
  "name": "智慧校园App开发",
  "description": "校园服务平台项目",
  "startDate": "2026-04-25",
  "endDate": "2026-08-30",
  "lifeCycleModel": "WATERFALL",
  "templateId": 1
}
```

## 6.3 项目详情

- `GET /projects/{projectId}`

## 6.4 更新项目

- `PUT /projects/{projectId}`

## 6.5 项目状态流转

- `PATCH /projects/{projectId}/status`

请求体：

```json
{
  "status": "IN_PROGRESS",
  "reason": "计划已评审通过"
}
```

## 6.6 关闭项目

- `POST /projects/{projectId}/close`

## 6.7 删除项目

- `DELETE /projects/{projectId}`

建议仅支持逻辑删除。

## 6.8 项目章程

- `GET /projects/{projectId}/charter`
- `POST /projects/{projectId}/charter`
- `PUT /projects/{projectId}/charter`

建议字段：

1. `objective`
2. `scopeSummary`
3. `sponsor`
4. `approver`
5. `stakeholders`
6. `successCriteria`

## 6.9 项目模板

- `GET /project-templates`
- `POST /project-templates`
- `GET /project-templates/{id}`
- `PUT /project-templates/{id}`

## 6.10 从模板创建项目

- `POST /projects/from-template`

## 6.11 初始化演示项目

- `POST /projects/init-demo`

仅管理员可调用。

## 7. 项目成员接口

## 7.1 成员列表

- `GET /projects/{projectId}/members`

## 7.2 添加成员

- `POST /projects/{projectId}/members`

请求体示例：

```json
{
  "userId": 2,
  "projectRole": "TEAM_MEMBER"
}
```

## 7.3 更新成员角色

- `PUT /projects/{projectId}/members/{memberId}`

## 7.4 移除成员

- `DELETE /projects/{projectId}/members/{memberId}`

## 8. 范围管理接口

## 8.1 WBS 列表

- `GET /projects/{projectId}/wbs`

## 8.2 新增 WBS 节点

- `POST /projects/{projectId}/wbs`

建议字段：

1. `parentId`
2. `code`
3. `name`
4. `description`
5. `ownerId`
6. `sortOrder`

## 8.3 更新 WBS 节点

- `PUT /projects/{projectId}/wbs/{id}`

## 8.4 删除 WBS 节点

- `DELETE /projects/{projectId}/wbs/{id}`

## 8.5 需求列表

- `GET /projects/{projectId}/requirements`

查询参数：

1. `type`
2. `priority`
3. `status`

## 8.6 创建需求

- `POST /projects/{projectId}/requirements`

## 8.7 更新需求

- `PUT /projects/{projectId}/requirements/{id}`

## 8.8 范围基准列表

- `GET /projects/{projectId}/scope-baselines`

## 8.9 创建范围基准

- `POST /projects/{projectId}/scope-baselines`

## 9. 进度管理接口

## 9.1 任务列表

- `GET /projects/{projectId}/tasks`

查询参数：

1. `assigneeId`
2. `status`
3. `milestoneId`
4. `keyword`

## 9.2 创建任务

- `POST /projects/{projectId}/tasks`

请求体示例：

```json
{
  "parentTaskId": null,
  "wbsId": 11,
  "name": "完成需求分析",
  "description": "输出需求说明书",
  "assigneeId": 3,
  "plannedStartDate": "2026-04-26",
  "plannedEndDate": "2026-05-02",
  "plannedHours": 24,
  "priority": "HIGH"
}
```

## 9.3 任务详情

- `GET /projects/{projectId}/tasks/{taskId}`

## 9.4 更新任务

- `PUT /projects/{projectId}/tasks/{taskId}`

## 9.5 删除任务

- `DELETE /projects/{projectId}/tasks/{taskId}`

## 9.6 更新任务进度

- `PATCH /projects/{projectId}/tasks/{taskId}/progress`

请求体：

```json
{
  "progress": 60,
  "status": "IN_PROGRESS",
  "remark": "原型设计已完成"
}
```

## 9.7 里程碑接口

- `GET /projects/{projectId}/milestones`
- `POST /projects/{projectId}/milestones`
- `PUT /projects/{projectId}/milestones/{id}`
- `DELETE /projects/{projectId}/milestones/{id}`

## 9.8 依赖关系接口

- `GET /projects/{projectId}/task-dependencies`
- `POST /projects/{projectId}/task-dependencies`
- `DELETE /projects/{projectId}/task-dependencies/{id}`

请求体示例：

```json
{
  "predecessorTaskId": 101,
  "successorTaskId": 102,
  "dependencyType": "FS"
}
```

## 9.9 甘特图数据

- `GET /projects/{projectId}/gantt`

## 9.10 关键路径数据

- `GET /projects/{projectId}/critical-path`

## 9.11 进度偏差预警

- `GET /projects/{projectId}/schedule-alerts`

## 10. 成本管理接口

## 10.1 成本计划列表

- `GET /projects/{projectId}/cost-plans`

## 10.2 新增成本计划

- `POST /projects/{projectId}/cost-plans`

字段建议：

1. `type`
2. `name`
3. `taskId`
4. `phase`
5. `plannedAmount`
6. `currency`
7. `remark`

## 10.3 更新成本计划

- `PUT /projects/{projectId}/cost-plans/{id}`

## 10.4 成本基准发布

- `POST /projects/{projectId}/cost-baselines`

## 10.5 实际成本列表

- `GET /projects/{projectId}/cost-actuals`

## 10.6 录入实际成本

- `POST /projects/{projectId}/cost-actuals`

## 10.7 EVM 指标

- `GET /projects/{projectId}/evm`

返回字段建议：

1. `pv`
2. `ev`
3. `ac`
4. `cv`
5. `sv`
6. `cpi`
7. `spi`

## 11. 质量与测试接口

## 11.1 质量计划

- `GET /projects/{projectId}/quality-plans`
- `POST /projects/{projectId}/quality-plans`
- `PUT /projects/{projectId}/quality-plans/{id}`

## 11.2 质量活动

- `GET /projects/{projectId}/quality-activities`
- `POST /projects/{projectId}/quality-activities`
- `PUT /projects/{projectId}/quality-activities/{id}`

## 11.3 质量指标

- `GET /projects/{projectId}/quality-metrics`
- `POST /projects/{projectId}/quality-metrics`

## 11.4 测试计划

- `GET /projects/{projectId}/test-plans`
- `POST /projects/{projectId}/test-plans`
- `PUT /projects/{projectId}/test-plans/{id}`

## 11.5 测试用例

- `GET /projects/{projectId}/test-cases`
- `POST /projects/{projectId}/test-cases`
- `PUT /projects/{projectId}/test-cases/{id}`

## 11.6 缺陷管理

- `GET /projects/{projectId}/defects`
- `POST /projects/{projectId}/defects`
- `PUT /projects/{projectId}/defects/{id}`

## 11.7 测试报告

- `GET /projects/{projectId}/test-reports`
- `POST /projects/{projectId}/test-reports/generate`

## 12. 配置与变更接口

## 12.1 配置项接口

- `GET /projects/{projectId}/config-items`
- `POST /projects/{projectId}/config-items`
- `PUT /projects/{projectId}/config-items/{id}`

## 12.2 基线接口

- `GET /projects/{projectId}/baselines`
- `POST /projects/{projectId}/baselines`

## 12.3 变更请求列表

- `GET /projects/{projectId}/change-requests`

查询参数：

1. `status`
2. `priority`
3. `proposerId`

## 12.4 提交变更请求

- `POST /projects/{projectId}/change-requests`

请求体示例：

```json
{
  "title": "增加数据导出PDF",
  "type": "SCOPE",
  "priority": "HIGH",
  "impactAnalysis": "影响前端报表模块和导出服务",
  "reason": "老师验收需要 PDF 版报告"
}
```

## 12.5 变更请求详情

- `GET /projects/{projectId}/change-requests/{id}`

## 12.6 审批变更请求

- `POST /projects/{projectId}/change-requests/{id}/approve`

请求体：

```json
{
  "decision": "APPROVED",
  "comment": "纳入当前迭代"
}
```

## 12.7 变更流转记录

- `GET /projects/{projectId}/change-requests/{id}/logs`

## 13. 风险管理接口

## 13.1 风险列表

- `GET /projects/{projectId}/risks`

查询参数：

1. `level`
2. `status`
3. `ownerId`

## 13.2 新增风险

- `POST /projects/{projectId}/risks`

请求体示例：

```json
{
  "name": "需求频繁变更",
  "probability": 4,
  "impact": 5,
  "level": "HIGH",
  "responseStrategy": "建立变更审批机制",
  "ownerId": 3
}
```

## 13.3 更新风险

- `PUT /projects/{projectId}/risks/{id}`

## 13.4 更新风险状态

- `PATCH /projects/{projectId}/risks/{id}/status`

## 13.5 风险矩阵数据

- `GET /projects/{projectId}/risk-matrix`

## 14. 沟通协作接口

## 14.1 沟通矩阵

- `GET /projects/{projectId}/communication-matrix`
- `POST /projects/{projectId}/communication-matrix`
- `PUT /projects/{projectId}/communication-matrix/{id}`

## 14.2 会议计划

- `GET /projects/{projectId}/meetings`
- `POST /projects/{projectId}/meetings`
- `PUT /projects/{projectId}/meetings/{id}`

## 14.3 沟通记录

- `GET /projects/{projectId}/communication-records`
- `POST /projects/{projectId}/communication-records`

## 14.4 任务评论

- `GET /projects/{projectId}/tasks/{taskId}/comments`
- `POST /projects/{projectId}/tasks/{taskId}/comments`
- `DELETE /projects/{projectId}/tasks/{taskId}/comments/{id}`

## 14.5 附件接口

- `POST /attachments/upload`
- `GET /attachments/{id}`
- `DELETE /attachments/{id}`

附件上传后可通过业务关联表绑定任务、会议、风险、变更等对象。

## 15. 采购与合同接口

## 15.1 供应商接口

- `GET /projects/{projectId}/suppliers`
- `POST /projects/{projectId}/suppliers`
- `PUT /projects/{projectId}/suppliers/{id}`

## 15.2 采购清单接口

- `GET /projects/{projectId}/procurements`
- `POST /projects/{projectId}/procurements`
- `PUT /projects/{projectId}/procurements/{id}`

## 15.3 合同接口

- `GET /projects/{projectId}/contracts`
- `POST /projects/{projectId}/contracts`
- `PUT /projects/{projectId}/contracts/{id}`

## 15.4 采购执行状态更新

- `PATCH /projects/{projectId}/procurements/{id}/status`

## 16. 工时管理接口

## 16.1 工时列表

- `GET /projects/{projectId}/timesheets`

查询参数：

1. `userId`
2. `taskId`
3. `startDate`
4. `endDate`

## 16.2 填报工时

- `POST /projects/{projectId}/timesheets`

请求体示例：

```json
{
  "taskId": 101,
  "workDate": "2026-04-23",
  "hours": 6,
  "description": "完成原型设计和评审准备"
}
```

## 16.3 更新工时

- `PUT /projects/{projectId}/timesheets/{id}`

## 16.4 删除工时

- `DELETE /projects/{projectId}/timesheets/{id}`

## 16.5 工时报表

- `GET /projects/{projectId}/timesheet-reports`

## 17. 仪表盘、日历与报表接口

## 17.1 首页仪表盘

- `GET /dashboard/home`

## 17.2 项目仪表盘

- `GET /projects/{projectId}/dashboard`

返回建议字段：

1. `projectCount`
2. `inProgressProjectCount`
3. `taskTotal`
4. `taskCompleted`
5. `taskCompletionRate`
6. `plannedCost`
7. `actualCost`
8. `openRiskCount`
9. `pendingChangeCount`
10. `upcomingMilestones`

## 17.3 项目日历

- `GET /projects/{projectId}/calendar`

## 17.4 个人日历

- `GET /calendar/my`

## 17.5 周报/月报生成

- `POST /projects/{projectId}/reports/generate`

请求体：

```json
{
  "type": "WEEKLY",
  "startDate": "2026-04-20",
  "endDate": "2026-04-26"
}
```

## 17.6 报表列表

- `GET /projects/{projectId}/reports`

## 17.7 项目总结报告生成

- `POST /projects/{projectId}/summary-report/generate`

## 18. 通知接口

## 18.1 通知列表

- `GET /notifications`

## 18.2 标记已读

- `PATCH /notifications/{id}/read`

## 18.3 全部已读

- `PATCH /notifications/read-all`

## 19. 项目收尾接口

## 19.1 验收清单

- `GET /projects/{projectId}/acceptance-items`
- `POST /projects/{projectId}/acceptance-items`
- `PUT /projects/{projectId}/acceptance-items/{id}`

## 19.2 文档归档

- `GET /projects/{projectId}/archives`
- `POST /projects/{projectId}/archives`

## 19.3 经验教训

- `GET /projects/{projectId}/lessons-learned`
- `POST /projects/{projectId}/lessons-learned`
- `PUT /projects/{projectId}/lessons-learned/{id}`

## 19.4 关闭前检查

- `GET /projects/{projectId}/closure-check`

返回建议字段：

1. `acceptanceCompleted`
2. `archiveCompleted`
3. `openRisks`
4. `pendingChanges`
5. `requiredReportsReady`

## 19.5 关闭项目

- `POST /projects/{projectId}/close`

## 20. 导入导出与基础能力接口

## 20.1 数据导出

- `POST /exports`

请求体示例：

```json
{
  "projectId": 1,
  "module": "TASK",
  "format": "EXCEL",
  "filters": {
    "status": "IN_PROGRESS"
  }
}
```

支持模块建议：

1. `TASK`
2. `RISK`
3. `COST`
4. `TIMESHEET`
5. `REPORT`

## 20.2 Excel 导入

- `POST /imports/excel`

首版可只支持 `WBS`、`TASK`、`RISK` 三类导入。

## 20.3 全局搜索

- `GET /search`

查询参数：

1. `keyword`
2. `type`
3. `projectId`

## 20.4 操作日志

- `GET /audit-logs`

查询参数：

1. `projectId`
2. `module`
3. `operatorId`
4. `startTime`
5. `endTime`

## 21. 关键状态与枚举建议

## 21.1 项目状态

1. `PLANNING`
2. `IN_PROGRESS`
3. `MONITORING`
4. `CLOSED`

## 21.2 任务状态

1. `TODO`
2. `IN_PROGRESS`
3. `BLOCKED`
4. `DONE`

## 21.3 变更状态

1. `SUBMITTED`
2. `UNDER_REVIEW`
3. `APPROVED`
4. `REJECTED`
5. `IMPLEMENTED`

## 21.4 风险状态

1. `IDENTIFIED`
2. `ANALYZED`
3. `RESPONDING`
4. `CLOSED`

## 21.5 缺陷状态

1. `NEW`
2. `PROCESSING`
3. `FIXED`
4. `CLOSED`

## 22. 非功能接口要求

1. 所有写接口必须记录审计日志。
2. 所有列表接口必须支持分页。
3. 所有项目级接口必须校验项目成员权限。
4. 删除接口建议逻辑删除。
5. 导出和报表生成建议异步化，返回任务 ID。
6. 附件上传需限制文件大小和类型。

## 23. 建议实施顺序

### 第一批接口

1. 认证与用户
2. 项目
3. 项目成员
4. WBS、需求
5. 任务、里程碑、依赖
6. 风险
7. 成本
8. 变更
9. 工时
10. 仪表盘
11. 结项

### 第二批接口

1. 质量与测试
2. 沟通协作
3. 采购合同
4. 通知
5. 日历
6. 导入导出

以上接口定义已覆盖文档中的核心功能点，并为推荐功能预留了统一接口结构，可直接作为后端 API 基线。
