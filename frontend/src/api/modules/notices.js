import { api } from '../index'

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response
}

const noticeApi = {
  list() {
    return api.get('/notices').then(unwrap)
  },
}

export { noticeApi }
