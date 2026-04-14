import { computed } from 'vue'
import { useUserStore } from '@/stores/modules/user'

export function useAuth() {
  const userStore = useUserStore()

  return {
    userStore,
    isAuthenticated: computed(() => userStore.isAuthenticated),
    isAdmin: computed(() => userStore.isAdmin),
    login: userStore.login,
    logout: userStore.logout,
  }
}
