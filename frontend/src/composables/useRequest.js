import { ref } from 'vue'

export function useRequest(requestFn, options = {}) {
  const data = ref(options.initialData ?? null)
  const loading = ref(false)
  const error = ref('')

  async function run(...args) {
    loading.value = true
    error.value = ''

    try {
      const response = await requestFn(...args)
      data.value = options.select ? options.select(response) : response
      return data.value
    } catch (requestError) {
      error.value = requestError instanceof Error ? requestError.message : 'request-failed'
      if (Object.prototype.hasOwnProperty.call(options, 'fallback')) {
        data.value = typeof options.fallback === 'function'
          ? options.fallback()
          : options.fallback
      }
      return data.value
    } finally {
      loading.value = false
    }
  }

  return {
    data,
    loading,
    error,
    run,
  }
}
