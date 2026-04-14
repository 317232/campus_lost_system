import { api } from '../index'

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response
}

const claimApi = {
  listMine() {
    return api.get('/users/me/claims').then(unwrap)
  },
}

export { claimApi }
