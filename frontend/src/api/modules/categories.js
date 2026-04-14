import { api } from '../index'

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response
}

const categoriesApi = {
  list() {
    return api.get('/categories').then(unwrap)
  },
}

export { categoriesApi }
