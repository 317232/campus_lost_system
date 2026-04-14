import { api } from '../index'

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response
}

const authApi = {
  login(payload) {
    return api.post('/auth/login', payload, { skipAuth: true }).then(unwrap)
  },
  register(payload) {
    return api.post('/auth/register', payload, { skipAuth: true }).then(unwrap)
  },
  forgotPassword(payload) {
    return api.post('/auth/forgot-password', payload, { skipAuth: true }).then(unwrap)
  },
  resetPassword(payload) {
    return api.post('/auth/reset-password', payload, { skipAuth: true }).then(unwrap)
  },
}

export { authApi }
