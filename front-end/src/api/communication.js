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

export function getProjectCommunicationMatrix(projectId, params = {}) {
  return http(`/projects/${projectId}/communication-matrix${buildQuery(params)}`)
}

export function createProjectCommunicationMatrix(projectId, payload) {
  return http(`/projects/${projectId}/communication-matrix`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectCommunicationMatrix(projectId, matrixId, payload) {
  return http(`/projects/${projectId}/communication-matrix/${matrixId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectCommunicationMatrix(projectId, matrixId) {
  return http(`/projects/${projectId}/communication-matrix/${matrixId}`, {
    method: 'DELETE',
  })
}

export function getProjectMeetings(projectId, params = {}) {
  return http(`/projects/${projectId}/meetings${buildQuery(params)}`)
}

export function createProjectMeeting(projectId, payload) {
  return http(`/projects/${projectId}/meetings`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectMeeting(projectId, meetingId, payload) {
  return http(`/projects/${projectId}/meetings/${meetingId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectMeeting(projectId, meetingId) {
  return http(`/projects/${projectId}/meetings/${meetingId}`, {
    method: 'DELETE',
  })
}

export function getProjectCommunicationRecords(projectId, params = {}) {
  return http(`/projects/${projectId}/communication-records${buildQuery(params)}`)
}

export function createProjectCommunicationRecord(projectId, payload) {
  return http(`/projects/${projectId}/communication-records`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProjectCommunicationRecord(projectId, recordId, payload) {
  return http(`/projects/${projectId}/communication-records/${recordId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProjectCommunicationRecord(projectId, recordId) {
  return http(`/projects/${projectId}/communication-records/${recordId}`, {
    method: 'DELETE',
  })
}
