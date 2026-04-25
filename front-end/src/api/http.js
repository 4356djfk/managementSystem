export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
const SESSION_EXPIRED_REDIRECT_FLAG = 'pm_session_expired_redirecting'

export async function http(path, options = {}) {
  const headers = new Headers(options.headers || {})
  const token = localStorage.getItem('pm_auth_token')
  const isFormData = options.body instanceof FormData

  if (!isFormData && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

  if (!response.ok || (payload && typeof payload === 'object' && 'code' in payload && !isSuccessCode(payload.code))) {
    const message = extractErrorMessage(payload) || `Request failed: ${response.status}`
    const error = new Error(message)
    error.payload = payload
    error.status = response.status

    if (response.status === 401 || payload?.code === 401) {
      localStorage.removeItem('pm_auth_token')
      localStorage.removeItem('pm_auth_user')
      forceLogoutRedirect()
    }

    throw error
  }

  if (payload && typeof payload === 'object' && 'data' in payload) {
    return payload.data
  }

  return payload
}

function extractErrorMessage(payload) {
  if (typeof payload === 'string') {
    return payload
  }

  if (payload && typeof payload === 'object') {
    return payload.message || payload.msg || ''
  }

  return ''
}

function isSuccessCode(code) {
  return code === 0 || code === 200 || code === '0' || code === '200'
}

function forceLogoutRedirect() {
  if (typeof window === 'undefined') return

  if (sessionStorage.getItem(SESSION_EXPIRED_REDIRECT_FLAG) === '1') {
    return
  }

  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
  if (window.location.pathname === '/login') {
    return
  }

  sessionStorage.setItem(SESSION_EXPIRED_REDIRECT_FLAG, '1')
  const redirect = encodeURIComponent(currentPath)
  window.location.replace(`/login?expired=1&redirect=${redirect}`)
}
