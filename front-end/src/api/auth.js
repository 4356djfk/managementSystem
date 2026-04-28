import { http } from './http'

export function login(payload) {
  return http('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function register(payload) {
  return http('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function logout() {
  return http('/auth/logout', {
    method: 'POST',
  })
}

export function getCurrentUser() {
  return http('/auth/me')
}

export function updateCurrentUser(payload) {
  return http('/auth/me', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}
