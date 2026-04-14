import { useUserStore } from '@/stores/modules/user'

export function installRouterGuards(router, pinia) {
  router.beforeEach((to) => {
    const userStore = useUserStore(pinia)

    if (!userStore.initialized) {
      userStore.hydrate()
    }

    if (to.meta?.guestOnly && userStore.isAuthenticated) {
      return userStore.isAdmin ? { name: 'admin-dashboard' } : { name: 'home' }
    }

    if (to.meta?.requiresAuth && !userStore.isAuthenticated) {
      return {
        name: 'login',
        query: { redirect: to.fullPath },
      }
    }

    if (to.meta?.roles?.length && !to.meta.roles.includes(userStore.role)) {
      return userStore.isAuthenticated ? { name: 'home' } : { name: 'login' }
    }

    return true
  })
}
