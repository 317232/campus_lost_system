import MainLayout from '@/layouts/MainLayout.vue'
import ClaimsView from '@/views/user/ClaimsView.vue'
import FoundDetailView from '@/views/user/FoundDetailView.vue'
import FoundListView from '@/views/user/FoundListView.vue'
import HomeView from '@/views/user/HomeView.vue'
import LostDetailView from '@/views/user/LostDetailView.vue'
import LostListView from '@/views/user/LostListView.vue'
import MyReportsView from '@/views/user/MyReportsView.vue'
import NoticeDetailView from '@/views/user/NoticeDetailView.vue'
import NoticeListView from '@/views/user/NoticeListView.vue'
import ProfileView from '@/views/user/ProfileView.vue'
import PublishView from '@/views/user/PublishView.vue'
import ReportView from '@/views/user/ReportView.vue'

export const portalRoutes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', name: 'home', component: HomeView },
      { path: 'lost', name: 'lost-list', component: LostListView },
      { path: 'lost/:id', name: 'lost-detail', component: LostDetailView },
      { path: 'found', name: 'found-list', component: FoundListView },
      { path: 'found/:id', name: 'found-detail', component: FoundDetailView },
      { path: 'publish/:mode', name: 'publish', component: PublishView },
      { path: 'claims', name: 'claims', component: ClaimsView, meta: { requiresAuth: true } },
      { path: 'profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
      { path: 'report', name: 'report', component: ReportView, meta: { requiresAuth: true } },
      { path: 'my-reports', name: 'my-reports', component: MyReportsView, meta: { requiresAuth: true } },
      { path: 'notices', name: 'notices', component: NoticeListView },
      { path: 'notices/:id', name: 'notice-detail', component: NoticeDetailView },
    ],
  },
]
