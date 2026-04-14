import { computed, onMounted, proxyRefs } from 'vue'
import { api, mockMode } from '@/api'
import { useRequest } from './useRequest'

export function useRemoteCollection(source, fallback, options = {}) {
  const fallbackItems = Array.isArray(fallback) ? fallback : []
  const requestFn = typeof source === 'function'
    ? source
    : () => api.get(source)

  const requestState = useRequest(requestFn, {
    fallback: fallbackItems,
    select: (response) => {
      const source = response?.data ?? response
      const payload = options.select ? options.select(source) : source?.data ?? source
      return Array.isArray(payload) ? payload : fallbackItems
    },
  })

  async function load() {
    if (mockMode) {
      requestState.data.value = fallbackItems
      requestState.error.value = ''
      requestState.loading.value = false
      return fallbackItems
    }

    return requestState.run()
  }

  onMounted(load)

  return proxyRefs({
    items: computed(() => requestState.data.value || fallbackItems),
    loading: requestState.loading,
    error: requestState.error,
    reload: load,
  })
}
