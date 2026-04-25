import { http } from './http'

export function getProjects(params = {}) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })

  const query = search.toString()
  return http(`/projects${query ? `?${query}` : ''}`)
}

export function createProject(payload) {
  return http('/projects', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectDetail(projectId) {
  return http(`/projects/${projectId}`)
}

export function updateProject(projectId, payload) {
  return http(`/projects/${projectId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function changeProjectStatus(projectId, payload) {
  return http(`/projects/${projectId}/status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function deleteProject(projectId) {
  return http(`/projects/${projectId}`, {
    method: 'DELETE',
  })
}

export function getProjectDashboard(projectId) {
  return http(`/projects/${projectId}/dashboard`)
}

export function getProjectCharter(projectId) {
  return http(`/projects/${projectId}/charter`)
}

export function saveProjectCharter(projectId, payload, charterId) {
  return http(`/projects/${projectId}/charter`, {
    method: charterId ? 'PUT' : 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProjectTemplates(params = {}) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })

  const query = search.toString()
  return http(`/project-templates${query ? `?${query}` : ''}`)
}

export function createProjectFromTemplate(payload) {
  return http('/projects/from-template', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function initDemoProject() {
  return http('/projects/init-demo', {
    method: 'POST',
  })
}

export function closeProject(projectId) {
  return http(`/projects/${projectId}/close`, {
    method: 'POST',
  })
}

export function getProjectMembers(projectId) {
  return http(`/projects/${projectId}/members`)
}

export function addProjectMember(projectId, payload) {
  return http(`/projects/${projectId}/members`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectMember(projectId, memberId, payload) {
  return http(`/projects/${projectId}/members/${memberId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function removeProjectMember(projectId, memberId) {
  return http(`/projects/${projectId}/members/${memberId}`, {
    method: 'DELETE',
  })
}
