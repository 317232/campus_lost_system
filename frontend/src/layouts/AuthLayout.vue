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
      <header class="auth-layout-header">
        <RouterLink class="auth-layout-home" to="/">← 返回首页</RouterLink>
        <div class="auth-layout-copy">
          <p class="eyebrow">Campus Lost &amp; Found</p>
          <h1>账号入口</h1>
          <p class="section-copy">登录、注册与找回密码都保持在同一组入口里，方便随时返回首页继续浏览。</p>
        </div>
        <nav class="auth-layout-nav" aria-label="认证页面导航">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            :class="['auth-layout-nav-link', { 'is-active': currentPath === item.to }]"
          >
            {{ item.label }}
          </RouterLink>
        </nav>
      </header>

      <router-view />
    </div>
  </div>
</template>
