import { api } from '../index'

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response
}

const userApi = {
  getProfile() {
    return api.get('/users/me').then(unwrap)
  },
  updateProfile(payload) {
    return api.put('/users/me', payload).then(unwrap)
  },
  getMyLostItems() {
    return api.get('/users/me/lost-items').then(unwrap)
  },
  getMyFoundItems() {
    return api.get('/users/me/found-items').then(unwrap)
  },
}

export { userApi }
