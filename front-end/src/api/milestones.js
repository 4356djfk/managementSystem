import { http } from './http'

export function getProjectMilestones(projectId) {
  return http(`/projects/${projectId}/milestones`)
}

export function createProjectMilestone(projectId, payload) {
  return http(`/projects/${projectId}/milestones`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectMilestone(projectId, milestoneId, payload) {
  return http(`/projects/${projectId}/milestones/${milestoneId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectMilestone(projectId, milestoneId) {
  return http(`/projects/${projectId}/milestones/${milestoneId}`, {
    method: 'DELETE',
  })
}
