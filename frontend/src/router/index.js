import { createRouter, createWebHistory } from 'vue-router'
import { pinia } from '@/stores'
import { adminRoutes } from './modules/admin'
import { authRoutes } from './modules/auth'
import { portalRoutes } from './modules/portal'
import { installRouterGuards } from './guards'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...portalRoutes,
    ...authRoutes,
    ...adminRoutes,
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

installRouterGuards(router, pinia)

export default router
