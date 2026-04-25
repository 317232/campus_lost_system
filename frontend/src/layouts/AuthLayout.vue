<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { to: '/login', label: '登录' },
  { to: '/register', label: '注册' },
  { to: '/forgot-password', label: '找回密码' },
]

const currentPath = computed(() => route.path)
</script>

<template>
  <div class="auth-layout-shell">
    <div class="auth-layout-card">
      <RouterLink class="auth-layout-home" to="/">返回首页</RouterLink>
      <header class="auth-layout-header">
        <nav class="auth-layout-nav" aria-label="认证页面导航">
          <RouterLink v-for="item in navItems" :key="item.to" :to="item.to"
            :class="['auth-layout-nav-link', { 'is-active': currentPath === item.to }]">
            {{ item.label }}
          </RouterLink>
        </nav>
      </header>

      <router-view />
    </div>
  </div>
</template>
