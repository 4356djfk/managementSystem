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

export function getProjectSuppliers(projectId, params = {}) {
  return http(`/projects/${projectId}/suppliers${buildQuery(params)}`)
}

export function createProjectSupplier(projectId, payload) {
  return http(`/projects/${projectId}/suppliers`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectSupplier(projectId, supplierId, payload) {
  return http(`/projects/${projectId}/suppliers/${supplierId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectSupplier(projectId, supplierId) {
  return http(`/projects/${projectId}/suppliers/${supplierId}`, {
    method: 'DELETE',
  })
}

export function getProjectProcurements(projectId, params = {}) {
  return http(`/projects/${projectId}/procurements${buildQuery(params)}`)
}

export function createProjectProcurement(projectId, payload) {
  return http(`/projects/${projectId}/procurements`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectProcurement(projectId, procurementId, payload) {
  return http(`/projects/${projectId}/procurements/${procurementId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function updateProjectProcurementStatus(projectId, procurementId, payload) {
  return http(`/projects/${projectId}/procurements/${procurementId}/status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectProcurement(projectId, procurementId) {
  return http(`/projects/${projectId}/procurements/${procurementId}`, {
    method: 'DELETE',
  })
}

export function getProjectContracts(projectId, params = {}) {
  return http(`/projects/${projectId}/contracts${buildQuery(params)}`)
}

export function createProjectContract(projectId, payload) {
  return http(`/projects/${projectId}/contracts`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectContract(projectId, contractId, payload) {
  return http(`/projects/${projectId}/contracts/${contractId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectContract(projectId, contractId) {
  return http(`/projects/${projectId}/contracts/${contractId}`, {
    method: 'DELETE',
  })
}
