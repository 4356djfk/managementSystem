import { http } from './http'

export function getProjectWbs(projectId) {
  return http(`/projects/${projectId}/wbs`)
}

export function createProjectWbsNode(projectId, payload) {
  return http(`/projects/${projectId}/wbs`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectRequirements(projectId, params = {}) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })

  const query = search.toString()
  return http(`/projects/${projectId}/requirements${query ? `?${query}` : ''}`)
}

export function createProjectRequirement(projectId, payload) {
  return http(`/projects/${projectId}/requirements`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectScopeBaselines(projectId) {
  return http(`/projects/${projectId}/scope-baselines`)
}

export function createProjectScopeBaseline(projectId, payload) {
  return http(`/projects/${projectId}/scope-baselines`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectScopeBaseline(projectId, baselineId) {
  return http(`/projects/${projectId}/scope-baselines/${baselineId}`, {
    method: 'DELETE',
  })
}
