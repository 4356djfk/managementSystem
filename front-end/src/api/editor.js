import { http } from './http'

export function getProjectEditorPreferences(projectId) {
  return http(`/projects/${projectId}/editor-preferences`)
}

export function saveProjectEditorPreferences(projectId, payload) {
  return http(`/projects/${projectId}/editor-preferences`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function getProjectCalendar(projectId) {
  return http(`/projects/${projectId}/calendar`)
}

export function getMyCalendar() {
  return http('/calendar/my')
}

export function getProjectCriticalPath(projectId) {
  return http(`/projects/${projectId}/critical-path`)
}

export function getProjectScheduleAlerts(projectId) {
  return http(`/projects/${projectId}/schedule-alerts`)
}

export function getProjectReports(projectId) {
  return http(`/projects/${projectId}/reports`)
}

export function generateProjectReport(projectId, payload) {
  return http(`/projects/${projectId}/reports/generate`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectReport(projectId, id) {
  return http(`/projects/${projectId}/reports/${id}`, {
    method: 'DELETE',
  })
}

export function getProjectTimesheetReport(projectId) {
  return http(`/projects/${projectId}/timesheet-reports`)
}

export function getProjectTimesheets(projectId, params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return http(`/projects/${projectId}/timesheets${query ? `?${query}` : ''}`)
}

export function createProjectTimesheet(projectId, payload) {
  return http(`/projects/${projectId}/timesheets`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectTimesheet(projectId, id, payload) {
  return http(`/projects/${projectId}/timesheets/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectTimesheet(projectId, id) {
  return http(`/projects/${projectId}/timesheets/${id}`, {
    method: 'DELETE',
  })
}

export function getProjectClosureCheck(projectId) {
  return http(`/projects/${projectId}/closure-check`)
}

export function getProjectArchives(projectId) {
  return http(`/projects/${projectId}/archives`)
}

export function createProjectArchive(projectId, payload) {
  return http(`/projects/${projectId}/archives`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectArchive(projectId, id) {
  return http(`/projects/${projectId}/archives/${id}`, {
    method: 'DELETE',
  })
}

export function getProjectLessonsLearned(projectId) {
  return http(`/projects/${projectId}/lessons-learned`)
}

export function createProjectLessonLearned(projectId, payload) {
  return http(`/projects/${projectId}/lessons-learned`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectLessonLearned(projectId, id, payload) {
  return http(`/projects/${projectId}/lessons-learned/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectLessonLearned(projectId, id) {
  return http(`/projects/${projectId}/lessons-learned/${id}`, {
    method: 'DELETE',
  })
}

export function generateProjectSummaryReport(projectId, payload) {
  return http(`/projects/${projectId}/summary-report/generate`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function uploadProjectAttachment(projectId, file) {
  const form = new FormData()
  form.append('file', file)
  return http(`/projects/${projectId}/attachments/upload`, {
    method: 'POST',
    body: form,
  })
}

export function deleteProjectAttachment(id) {
  return http(`/attachments/${id}`, {
    method: 'DELETE',
  })
}
