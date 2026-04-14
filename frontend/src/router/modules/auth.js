import AuthLayout from '@/layouts/AuthLayout.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'

export const authRoutes = [
  {
    path: '/',
    component: AuthLayout,
    children: [
      { path: 'login', name: 'login', component: LoginView, meta: { guestOnly: true } },
      { path: 'register', name: 'register', component: RegisterView, meta: { guestOnly: true } },
      { path: 'forgot-password', name: 'forgot-password', component: ForgotPasswordView, meta: { guestOnly: true } },
    ],
  },
]
