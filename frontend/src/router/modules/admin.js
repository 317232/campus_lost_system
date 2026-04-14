import AdminLayout from '@/layouts/AdminLayout.vue'
import AdminDashboardView from '@/views/admin/AdminDashboardView.vue'
import AdminNoticesView from '@/views/admin/AdminNoticesView.vue'
import AdminReportsView from '@/views/admin/AdminReportsView.vue'
import AdminReviewView from '@/views/admin/AdminReviewView.vue'
import AdminStatisticsView from '@/views/admin/AdminStatisticsView.vue'
import AdminUsersView from '@/views/admin/AdminUsersView.vue'

export const adminRoutes = [
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: '', name: 'admin-dashboard', component: AdminDashboardView },
      { path: 'reviews', name: 'admin-reviews', component: AdminReviewView },
      { path: 'users', name: 'admin-users', component: AdminUsersView },
      { path: 'reports', name: 'admin-reports', component: AdminReportsView },
      { path: 'notices', name: 'admin-notices', component: AdminNoticesView },
      { path: 'statistics', name: 'admin-statistics', component: AdminStatisticsView },
    ],
  },
]
