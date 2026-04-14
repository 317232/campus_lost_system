import { api } from '../index'

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response
}

const adminApi = {
  getOverview() {
    return api.get('/admin/statistics/overview').then(unwrap)
  },
  getTrend() {
    return api.get('/admin/statistics/trend').then(unwrap)
  },
  getReviewQueue() {
    return api.get('/admin/lost-items/review').then(unwrap)
  },
  getUsers() {
    return api.get('/admin/users').then(unwrap)
  },
  getReports() {
    return api.get('/admin/reports').then(unwrap)
  },
  getNotices() {
    return api.get('/admin/notices').then(unwrap)
  },
}

export { adminApi }
