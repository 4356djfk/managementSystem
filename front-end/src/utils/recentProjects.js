const STORAGE_KEY = 'pm_recent_projects'
const MAX_RECENT_PROJECTS = 6
export const RECENT_PROJECTS_UPDATED_EVENT = 'pm:recent-projects-updated'

function normalizeRecentProject(item) {
  const projectId = item?.projectId ?? item?.id
  const name = String(item?.name || '').trim()
  const lastOpenedAt = String(item?.lastOpenedAt || '').trim()

  if (projectId == null || !name || !lastOpenedAt) {
    return null
  }

  return {
    projectId: String(projectId),
    name,
    lastOpenedAt,
  }
}

function emitRecentProjectsUpdated(items) {
  if (typeof window === 'undefined' || typeof window.dispatchEvent !== 'function') {
    return
  }

  window.dispatchEvent(new CustomEvent(RECENT_PROJECTS_UPDATED_EVENT, { detail: items }))
}

export function loadRecentProjects() {
  if (typeof window === 'undefined') {
    return []
  }

  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return []
  }

  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed
      .map(normalizeRecentProject)
      .filter(Boolean)
      .slice(0, MAX_RECENT_PROJECTS)
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
    return []
  }
}

export function saveRecentProjects(items) {
  if (typeof window === 'undefined') {
    return []
  }

  const normalized = Array.isArray(items)
    ? items.map(normalizeRecentProject).filter(Boolean).slice(0, MAX_RECENT_PROJECTS)
    : []

  if (normalized.length) {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(normalized))
  } else {
    window.localStorage.removeItem(STORAGE_KEY)
  }

  emitRecentProjectsUpdated(normalized)
  return normalized
}

export function recordRecentProject(project) {
  const normalized = normalizeRecentProject({
    ...project,
    lastOpenedAt: new Date().toISOString(),
  })

  if (!normalized) {
    return loadRecentProjects()
  }

  const existing = loadRecentProjects().filter((item) => item.projectId !== normalized.projectId)
  return saveRecentProjects([normalized, ...existing])
}
