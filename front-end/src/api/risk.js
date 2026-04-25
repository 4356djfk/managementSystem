import { http } from './http'

export function getProjectRisks(projectId, params = {}) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })

  const query = search.toString()
  return http(`/projects/${projectId}/risks${query ? `?${query}` : ''}`)
}

export function createProjectRisk(projectId, payload) {
  return http(`/projects/${projectId}/risks`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectRisk(projectId, riskId, payload) {
  return http(`/projects/${projectId}/risks/${riskId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function updateProjectRiskStatus(projectId, riskId, payload) {
  return http(`/projects/${projectId}/risks/${riskId}/status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectRisk(projectId, riskId) {
  return http(`/projects/${projectId}/risks/${riskId}`, {
    method: 'DELETE',
  })
}

export function getProjectRiskMatrix(projectId) {
  return http(`/projects/${projectId}/risk-matrix`)
}
