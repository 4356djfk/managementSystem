import { http } from './http'

export function getAcceptanceItems(projectId) {
  return http(`/projects/${projectId}/acceptance-items`)
}

export function createAcceptanceItem(projectId, payload) {
  return http(`/projects/${projectId}/acceptance-items`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateAcceptanceItem(projectId, id, payload) {
  return http(`/projects/${projectId}/acceptance-items/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteAcceptanceItem(projectId, id) {
  return http(`/projects/${projectId}/acceptance-items/${id}`, {
    method: 'DELETE',
  })
}

export function createExportTask(payload) {
  return http('/exports', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function importExcelFile(file, module, projectId) {
  const form = new FormData()
  form.append('file', file)
  form.append('module', module)
  form.append('projectId', String(projectId))
  return http('/imports/excel', {
    method: 'POST',
    body: form,
  })
}

export function searchGlobal(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return http(`/search${query ? `?${query}` : ''}`)
}

export function getAuditLogs(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })
  const query = search.toString()
  return http(`/audit-logs${query ? `?${query}` : ''}`)
}
