import { http } from './http'

export function getProjectChangeRequests(projectId, params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return http(`/projects/${projectId}/change-requests${query ? `?${query}` : ''}`)
}

export function createProjectChangeRequest(projectId, payload) {
  return http(`/projects/${projectId}/change-requests`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectChangeRequestDetail(projectId, id) {
  return http(`/projects/${projectId}/change-requests/${id}`)
}

export function approveProjectChangeRequest(projectId, id, payload) {
  return http(`/projects/${projectId}/change-requests/${id}/approve`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectChangeRequestLogs(projectId, id) {
  return http(`/projects/${projectId}/change-requests/${id}/logs`)
}
