import { http } from './http'

function buildQuery(params = {}) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })

  const query = search.toString()
  return query ? `?${query}` : ''
}

export function getProjectTestPlans(projectId, params = {}) {
  return http(`/projects/${projectId}/test-plans${buildQuery(params)}`)
}

export function createProjectTestPlan(projectId, payload) {
  return http(`/projects/${projectId}/test-plans`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectTestPlan(projectId, planId, payload) {
  return http(`/projects/${projectId}/test-plans/${planId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectTestPlan(projectId, planId) {
  return http(`/projects/${projectId}/test-plans/${planId}`, {
    method: 'DELETE',
  })
}

export function getProjectTestCases(projectId, params = {}) {
  return http(`/projects/${projectId}/test-cases${buildQuery(params)}`)
}

export function createProjectTestCase(projectId, payload) {
  return http(`/projects/${projectId}/test-cases`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectTestCase(projectId, caseId, payload) {
  return http(`/projects/${projectId}/test-cases/${caseId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectTestCase(projectId, caseId) {
  return http(`/projects/${projectId}/test-cases/${caseId}`, {
    method: 'DELETE',
  })
}

export function getProjectDefects(projectId, params = {}) {
  return http(`/projects/${projectId}/defects${buildQuery(params)}`)
}

export function createProjectDefect(projectId, payload) {
  return http(`/projects/${projectId}/defects`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectDefect(projectId, defectId, payload) {
  return http(`/projects/${projectId}/defects/${defectId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectDefect(projectId, defectId) {
  return http(`/projects/${projectId}/defects/${defectId}`, {
    method: 'DELETE',
  })
}

export function getProjectTestReports(projectId) {
  return http(`/projects/${projectId}/test-reports`)
}

export function generateProjectTestReport(projectId, payload) {
  return http(`/projects/${projectId}/test-reports/generate`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectTestReport(projectId, reportId) {
  return http(`/projects/${projectId}/test-reports/${reportId}`, {
    method: 'DELETE',
  })
}
