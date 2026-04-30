# 软件项目管理系统 API 接口清单

## 1. 统一约定

### 1.1 基础说明

1. 当前后端未使用 `/api/v1` 前缀，接口直接暴露为根路径
2. 请求与响应格式默认为 `application/json`
3. 文件上传使用 `multipart/form-data`
4. 认证方式为 `Bearer Token`

### 1.2 统一返回

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {},
  "requestId": "uuid"
}
```

### 1.3 分页返回

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [],
    "page": 1,
    "pageSize": 20,
    "total": 0
  },
  "requestId": "uuid"
}
```

## 2. 认证与用户

### 2.1 认证

1. `POST /auth/register`
2. `POST /auth/login`
3. `POST /auth/logout`
4. `GET /auth/me`
5. `PUT /auth/me`

### 2.2 用户管理

1. `GET /users`
2. `POST /users`
3. `PUT /users/{id}`
4. `PATCH /users/{id}/status`
5. `PUT /users/{id}/roles`

## 3. 项目与模板

### 3.1 项目

1. `GET /projects`
2. `GET /projects/{projectId}`
3. `POST /projects`
4. `PUT /projects/{projectId}`
5. `PATCH /projects/{projectId}/status`
6. `POST /projects/{projectId}/close`
7. `DELETE /projects/{projectId}`
8. `GET /projects/{projectId}/charter`
9. `POST /projects/{projectId}/charter`
10. `PUT /projects/{projectId}/charter`
11. `POST /projects/from-template`
12. `POST /projects/init-demo`
13. `GET /projects/{projectId}/dashboard`

### 3.2 模板

1. `GET /project-templates`
2. `POST /project-templates`
3. `GET /project-templates/{id}`
4. `PUT /project-templates/{id}`

### 3.3 项目成员

1. `GET /projects/{projectId}/members`
2. `GET /projects/{projectId}/members/candidates`
3. `POST /projects/{projectId}/members`
4. `PUT /projects/{projectId}/members/{memberId}`
5. `DELETE /projects/{projectId}/members/{memberId}`

### 3.4 编辑器偏好

1. `GET /projects/{projectId}/editor-preferences`
2. `PUT /projects/{projectId}/editor-preferences`

## 4. 范围与进度

### 4.1 WBS

1. `GET /projects/{projectId}/wbs`
2. `POST /projects/{projectId}/wbs`
3. `PUT /projects/{projectId}/wbs/{id}`
4. `DELETE /projects/{projectId}/wbs/{id}`

### 4.2 需求

1. `GET /projects/{projectId}/requirements`
2. `POST /projects/{projectId}/requirements`
3. `PUT /projects/{projectId}/requirements/{id}`

### 4.3 范围基线

1. `GET /projects/{projectId}/scope-baselines`
2. `POST /projects/{projectId}/scope-baselines`
3. `DELETE /projects/{projectId}/scope-baselines/{id}`

### 4.4 任务

1. `GET /projects/{projectId}/tasks`
2. `POST /projects/{projectId}/tasks`
3. `GET /projects/{projectId}/tasks/{taskId}`
4. `PUT /projects/{projectId}/tasks/{taskId}`
5. `DELETE /projects/{projectId}/tasks/{taskId}`
6. `PATCH /projects/{projectId}/tasks/{taskId}/progress`

### 4.5 任务依赖与评论

1. `GET /projects/{projectId}/task-dependencies`
2. `POST /projects/{projectId}/task-dependencies`
3. `DELETE /projects/{projectId}/task-dependencies/{id}`
4. `POST /projects/{projectId}/tasks/{taskId}/comments`
5. `GET /projects/{projectId}/tasks/{taskId}/comments`
6. `DELETE /projects/{projectId}/tasks/{taskId}/comments/{id}`

### 4.6 甘特与进度分析

1. `GET /projects/{projectId}/gantt`
2. `GET /projects/{projectId}/critical-path`
3. `GET /projects/{projectId}/schedule-alerts`

### 4.7 里程碑

1. `GET /projects/{projectId}/milestones`
2. `POST /projects/{projectId}/milestones`
3. `PUT /projects/{projectId}/milestones/{id}`
4. `DELETE /projects/{projectId}/milestones/{id}`

## 5. 风险、成本、质量、测试

### 5.1 风险

1. `GET /projects/{projectId}/risks`
2. `POST /projects/{projectId}/risks`
3. `PUT /projects/{projectId}/risks/{id}`
4. `PATCH /projects/{projectId}/risks/{id}/status`
5. `DELETE /projects/{projectId}/risks/{id}`
6. `GET /projects/{projectId}/risk-matrix`

### 5.2 成本

1. `GET /projects/{projectId}/cost-plans`
2. `POST /projects/{projectId}/cost-plans`
3. `PUT /projects/{projectId}/cost-plans/{id}`
4. `DELETE /projects/{projectId}/cost-plans/{id}`
5. `POST /projects/{projectId}/cost-baselines`
6. `GET /projects/{projectId}/cost-baselines`
7. `DELETE /projects/{projectId}/cost-baselines/{id}`
8. `GET /projects/{projectId}/cost-actuals`
9. `POST /projects/{projectId}/cost-actuals`
10. `DELETE /projects/{projectId}/cost-actuals/{id}`
11. `GET /projects/{projectId}/evm`

### 5.3 质量

1. `GET /projects/{projectId}/quality-plans`
2. `POST /projects/{projectId}/quality-plans`
3. `PUT /projects/{projectId}/quality-plans/{id}`
4. `DELETE /projects/{projectId}/quality-plans/{id}`
5. `GET /projects/{projectId}/quality-activities`
6. `POST /projects/{projectId}/quality-activities`
7. `PUT /projects/{projectId}/quality-activities/{id}`
8. `DELETE /projects/{projectId}/quality-activities/{id}`
9. `GET /projects/{projectId}/quality-metrics`
10. `POST /projects/{projectId}/quality-metrics`
11. `PUT /projects/{projectId}/quality-metrics/{id}`
12. `DELETE /projects/{projectId}/quality-metrics/{id}`

### 5.4 测试

1. `GET /projects/{projectId}/test-plans`
2. `POST /projects/{projectId}/test-plans`
3. `PUT /projects/{projectId}/test-plans/{id}`
4. `DELETE /projects/{projectId}/test-plans/{id}`
5. `GET /projects/{projectId}/test-cases`
6. `POST /projects/{projectId}/test-cases`
7. `PUT /projects/{projectId}/test-cases/{id}`
8. `DELETE /projects/{projectId}/test-cases/{id}`
9. `GET /projects/{projectId}/defects`
10. `POST /projects/{projectId}/defects`
11. `PUT /projects/{projectId}/defects/{id}`
12. `DELETE /projects/{projectId}/defects/{id}`
13. `GET /projects/{projectId}/test-reports`
14. `POST /projects/{projectId}/test-reports/generate`
15. `DELETE /projects/{projectId}/test-reports/{id}`

## 6. 配置、变更、沟通、采购

### 6.1 配置管理

1. `GET /projects/{projectId}/config-items`
2. `POST /projects/{projectId}/config-items`
3. `PUT /projects/{projectId}/config-items/{id}`
4. `DELETE /projects/{projectId}/config-items/{id}`
5. `GET /projects/{projectId}/baselines`
6. `POST /projects/{projectId}/baselines`
7. `DELETE /projects/{projectId}/baselines/{id}`
8. `POST /projects/{projectId}/baselines/{id}/restore`

### 6.2 变更管理

1. `GET /projects/{projectId}/change-requests`
2. `POST /projects/{projectId}/change-requests`
3. `GET /projects/{projectId}/change-requests/{id}`
4. `POST /projects/{projectId}/change-requests/{id}/approve`
5. `GET /projects/{projectId}/change-requests/{id}/logs`

### 6.3 沟通管理

1. `GET /projects/{projectId}/communication-matrix`
2. `POST /projects/{projectId}/communication-matrix`
3. `PUT /projects/{projectId}/communication-matrix/{id}`
4. `DELETE /projects/{projectId}/communication-matrix/{id}`
5. `GET /projects/{projectId}/meetings`
6. `POST /projects/{projectId}/meetings`
7. `PUT /projects/{projectId}/meetings/{id}`
8. `DELETE /projects/{projectId}/meetings/{id}`
9. `GET /projects/{projectId}/communication-records`
10. `POST /projects/{projectId}/communication-records`
11. `PUT /projects/{projectId}/communication-records/{id}`
12. `DELETE /projects/{projectId}/communication-records/{id}`

### 6.4 采购与合同

1. `GET /projects/{projectId}/suppliers`
2. `POST /projects/{projectId}/suppliers`
3. `PUT /projects/{projectId}/suppliers/{id}`
4. `DELETE /projects/{projectId}/suppliers/{id}`
5. `GET /projects/{projectId}/procurements`
6. `POST /projects/{projectId}/procurements`
7. `PUT /projects/{projectId}/procurements/{id}`
8. `PATCH /projects/{projectId}/procurements/{id}/status`
9. `DELETE /projects/{projectId}/procurements/{id}`
10. `GET /projects/{projectId}/contracts`
11. `POST /projects/{projectId}/contracts`
12. `PUT /projects/{projectId}/contracts/{id}`
13. `DELETE /projects/{projectId}/contracts/{id}`

## 7. 工时、报表、结项

### 7.1 工时

1. `GET /projects/{projectId}/timesheets`
2. `POST /projects/{projectId}/timesheets`
3. `PUT /projects/{projectId}/timesheets/{id}`
4. `DELETE /projects/{projectId}/timesheets/{id}`
5. `GET /projects/{projectId}/timesheet-reports`

### 7.2 结项资料

1. `GET /projects/{projectId}/acceptance-items`
2. `POST /projects/{projectId}/acceptance-items`
3. `PUT /projects/{projectId}/acceptance-items/{id}`
4. `DELETE /projects/{projectId}/acceptance-items/{id}`
5. `GET /projects/{projectId}/archives`
6. `POST /projects/{projectId}/archives`
7. `DELETE /projects/{projectId}/archives/{id}`
8. `GET /projects/{projectId}/lessons-learned`
9. `POST /projects/{projectId}/lessons-learned`
10. `PUT /projects/{projectId}/lessons-learned/{id}`
11. `DELETE /projects/{projectId}/lessons-learned/{id}`

### 7.3 报表与附件

1. `POST /projects/{projectId}/reports/generate`
2. `GET /projects/{projectId}/reports`
3. `DELETE /projects/{projectId}/reports/{id}`
4. `POST /projects/{projectId}/summary-report/generate`
5. `POST /projects/{projectId}/attachments/upload`
6. `GET /attachments/{id}`
7. `GET /attachments/{id}/download`
8. `DELETE /attachments/{id}`

## 8. 全局能力

### 8.1 仪表盘与日历

1. `GET /dashboard/home`
2. `GET /projects/{projectId}/calendar`
3. `GET /calendar/my`
4. `GET /projects/{projectId}/closure-check`

### 8.2 通知、导入导出、搜索、审计

1. `GET /notifications`
2. `PATCH /notifications/{id}/read`
3. `PATCH /notifications/read-all`
4. `POST /exports`
5. `POST /imports/excel`
6. `GET /imports/excel/template`
7. `GET /search`
8. `GET /audit-logs`

## 9. 当前关键 DTO / VO 变化点

### 9.1 任务相关

`CreateTaskDto` / `UpdateTaskDto` 已包含：

1. `deadlineDate`
2. `constraintType`
3. `constraintDate`

`TaskDependency` 已包含：

1. `lagDays`

### 9.2 编辑器偏好

`SaveProjectEditorPreferenceDto` 与 `ProjectEditorPreferenceVO` 已支持：

1. `ganttAppearance`
2. `wbsConfig`
3. `scheduleOptions`

## 10. 说明

本文件以“当前代码已实现接口”为准，不再保留旧版 `/api/v1` 前缀设定。
