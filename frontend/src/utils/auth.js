const AUTH_STORAGE_KEY = 'campus-auth'

function getStorage() {
  if (typeof window === 'undefined') {
    return null
  }

  return window.localStorage
}

function normalizeSession(session = {}) {
  return {
    token: session.token || '',
    role: session.role || '',
    displayName: session.displayName || '',
    profile: session.profile || null,
  }
}

function readAuthSession() {
  const storage = getStorage()
  if (!storage) {
    return normalizeSession()
  }

  try {
    const raw = storage.getItem(AUTH_STORAGE_KEY)
    if (!raw) {
      return normalizeSession()
    }

    return normalizeSession(JSON.parse(raw))
  } catch {
    return normalizeSession()
  }
}

function saveAuthSession(session) {
  const storage = getStorage()
  if (!storage) {
    return normalizeSession(session)
  }

  const normalized = normalizeSession(session)
  storage.setItem(AUTH_STORAGE_KEY, JSON.stringify(normalized))
  return normalized
}

function clearAuthSession() {
  const storage = getStorage()
  if (storage) {
    storage.removeItem(AUTH_STORAGE_KEY)
  }
}

function getAuthToken() {
  return readAuthSession().token
}

function getAuthRole() {
  return readAuthSession().role
}

export {
  AUTH_STORAGE_KEY,
  clearAuthSession,
  getAuthRole,
  getAuthToken,
  readAuthSession,
  saveAuthSession,
}
