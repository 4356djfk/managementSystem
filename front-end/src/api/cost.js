import { http } from './http'

export function getProjectCostPlans(projectId) {
  return http(`/projects/${projectId}/cost-plans`)
}

export function createProjectCostPlan(projectId, payload) {
  return http(`/projects/${projectId}/cost-plans`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectCostPlan(projectId, planId, payload) {
  return http(`/projects/${projectId}/cost-plans/${planId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectCostPlan(projectId, planId) {
  return http(`/projects/${projectId}/cost-plans/${planId}`, {
    method: 'DELETE',
  })
}

export function getProjectCostActuals(projectId) {
  return http(`/projects/${projectId}/cost-actuals`)
}

export function createProjectCostActual(projectId, payload) {
  return http(`/projects/${projectId}/cost-actuals`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectCostActual(projectId, actualId) {
  return http(`/projects/${projectId}/cost-actuals/${actualId}`, {
    method: 'DELETE',
  })
}

export function getProjectCostBaselines(projectId) {
  return http(`/projects/${projectId}/cost-baselines`)
}

export function createProjectCostBaseline(projectId, payload) {
  return http(`/projects/${projectId}/cost-baselines`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectCostBaseline(projectId, baselineId) {
  return http(`/projects/${projectId}/cost-baselines/${baselineId}`, {
    method: 'DELETE',
  })
}

export function getProjectEvm(projectId) {
  return http(`/projects/${projectId}/evm`)
}
