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

export function getProjectQualityPlans(projectId, params = {}) {
  return http(`/projects/${projectId}/quality-plans${buildQuery(params)}`)
}

export function createProjectQualityPlan(projectId, payload) {
  return http(`/projects/${projectId}/quality-plans`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectQualityPlan(projectId, planId, payload) {
  return http(`/projects/${projectId}/quality-plans/${planId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectQualityPlan(projectId, planId) {
  return http(`/projects/${projectId}/quality-plans/${planId}`, {
    method: 'DELETE',
  })
}

export function getProjectQualityActivities(projectId, params = {}) {
  return http(`/projects/${projectId}/quality-activities${buildQuery(params)}`)
}

export function createProjectQualityActivity(projectId, payload) {
  return http(`/projects/${projectId}/quality-activities`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectQualityActivity(projectId, activityId, payload) {
  return http(`/projects/${projectId}/quality-activities/${activityId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectQualityActivity(projectId, activityId) {
  return http(`/projects/${projectId}/quality-activities/${activityId}`, {
    method: 'DELETE',
  })
}

export function getProjectQualityMetrics(projectId, params = {}) {
  return http(`/projects/${projectId}/quality-metrics${buildQuery(params)}`)
}

export function createProjectQualityMetric(projectId, payload) {
  return http(`/projects/${projectId}/quality-metrics`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectQualityMetric(projectId, metricId, payload) {
  return http(`/projects/${projectId}/quality-metrics/${metricId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectQualityMetric(projectId, metricId) {
  return http(`/projects/${projectId}/quality-metrics/${metricId}`, {
    method: 'DELETE',
  })
}
