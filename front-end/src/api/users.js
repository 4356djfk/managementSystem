import { http } from './http'

export function getUsers(params = {}) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value)
    }
  })

  const query = search.toString()
  return http(`/users${query ? `?${query}` : ''}`)
}

export function createUser(payload) {
  return http('/users', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateUser(id, payload) {
  return http(`/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function updateUserStatus(id, payload) {
  return http(`/users/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function updateUserRoles(id, payload) {
  return http(`/users/${id}/roles`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
