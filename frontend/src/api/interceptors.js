import { clearAuthSession, getAuthToken } from '@/utils/auth'

function normalizeError(error) {
  const fallback = new Error('请求失败，请稍后重试。')

  if (!error) {
    return fallback
  }

  const message =
    error.response?.data?.message ||
    error.response?.data?.error ||
    error.message ||
    fallback.message

  const normalized = new Error(message)
  normalized.status = error.response?.status || 0
  normalized.payload = error.response?.data || null
  return normalized
}

function applyInterceptors(http) {
  http.interceptors.request.use((config) => {
    const nextConfig = { ...config }
    const token = getAuthToken()

    if (token && !nextConfig.skipAuth) {
      nextConfig.headers = {
        ...(nextConfig.headers || {}),
        Authorization: `Bearer ${token}`,
      }
    }

    return nextConfig
  })

  http.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        clearAuthSession()
      }

      return Promise.reject(normalizeError(error))
    },
  )
}

export { applyInterceptors }
