import { api } from '../index'

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response
}

const itemApi = {
  getLostItems() {
    return api.get('/lost-items').then(unwrap)
  },
  getFoundItems() {
    return api.get('/found-items').then(unwrap)
  },
  getLostDetail(id) {
    return api.get(`/lost-items/${id}`).then(unwrap)
  },
  getFoundDetail(id) {
    return api.get(`/found-items/${id}`).then(unwrap)
  },
}

export { itemApi }
