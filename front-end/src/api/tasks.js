import { http } from './http'

export function getProjectTasks(projectId, params = {}) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })

  const query = search.toString()
  return http(`/projects/${projectId}/tasks${query ? `?${query}` : ''}`)
}

export function createProjectTask(projectId, payload) {
  return http(`/projects/${projectId}/tasks`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectTaskDetail(projectId, taskId) {
  return http(`/projects/${projectId}/tasks/${taskId}`)
}

export function updateProjectTask(projectId, taskId, payload) {
  return http(`/projects/${projectId}/tasks/${taskId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function updateProjectTaskProgress(projectId, taskId, payload) {
  return http(`/projects/${projectId}/tasks/${taskId}/progress`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function getProjectTaskDependencies(projectId) {
  return http(`/projects/${projectId}/task-dependencies`)
}

export function createProjectTaskDependency(projectId, payload) {
  return http(`/projects/${projectId}/task-dependencies`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectTaskDependency(projectId, dependencyId) {
  return http(`/projects/${projectId}/task-dependencies/${dependencyId}`, {
    method: 'DELETE',
  })
}

export function createProjectTaskComment(projectId, taskId, payload) {
  return http(`/projects/${projectId}/tasks/${taskId}/comments`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectTaskComments(projectId, taskId) {
  return http(`/projects/${projectId}/tasks/${taskId}/comments`)
}

export function deleteProjectTaskComment(projectId, taskId, commentId) {
  return http(`/projects/${projectId}/tasks/${taskId}/comments/${commentId}`, {
    method: 'DELETE',
  })
}

export function deleteProjectTask(projectId, taskId) {
  return http(`/projects/${projectId}/tasks/${taskId}`, {
    method: 'DELETE',
  })
}
