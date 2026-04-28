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

export function getProjectConfigItems(projectId, params = {}) {
  return http(`/projects/${projectId}/config-items${buildQuery(params)}`)
}

export function createProjectConfigItem(projectId, payload) {
  return http(`/projects/${projectId}/config-items`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectConfigItem(projectId, itemId, payload) {
  return http(`/projects/${projectId}/config-items/${itemId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectConfigItem(projectId, itemId) {
  return http(`/projects/${projectId}/config-items/${itemId}`, {
    method: 'DELETE',
  })
}

export function getProjectBaselines(projectId, params = {}) {
  return http(`/projects/${projectId}/baselines${buildQuery(params)}`)
}

export function createProjectBaseline(projectId, payload) {
  return http(`/projects/${projectId}/baselines`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectBaseline(projectId, baselineId) {
  return http(`/projects/${projectId}/baselines/${baselineId}`, {
    method: 'DELETE',
  })
}

export function restoreProjectBaseline(projectId, baselineId) {
  return http(`/projects/${projectId}/baselines/${baselineId}/restore`, {
    method: 'POST',
  })
}
