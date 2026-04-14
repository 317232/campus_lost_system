import axios from 'axios'
import { applyInterceptors } from './interceptors'

const API_BASE = import.meta.env.VITE_API_BASE || '/api'
const mockMode = import.meta.env.VITE_USE_MOCK === 'true'

const http = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

applyInterceptors(http)

const api = {
  get: (url, config) => http.get(url, config),
  post: (url, data, config) => http.post(url, data, config),
  put: (url, data, config) => http.put(url, data, config),
  delete: (url, config) => http.delete(url, config),
}

export { API_BASE, api, http, mockMode }
