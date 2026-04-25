<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'

const route = useRoute()
const userStore = useUserStore()

const links = [
  { label: '数据统计', to: '/admin', hint: '平台概览与数据分析', icon: '📊' },
  { label: '用户管理', to: '/admin/users', hint: '账号管理与角色权限', icon: '👥' },
  { label: '物品管理', to: '/admin/reviews', hint: '失物招领审核与处置', icon: '📦' },
  { label: '举报反馈', to: '/admin/reports', hint: '用户反馈与问题处理', icon: '⚠️' },
  { label: '公告管理', to: '/admin/notices', hint: '系统通知与公告发布', icon: '📢' },
  { label: '分类管理', to: '/admin/categories', hint: '物品分类维护与设置', icon: '🏷️' },
  { label: '系统日志', to: '/admin/statistics', hint: '操作记录与审计追踪', icon: '📋' },
]

const currentLink = computed(() => {
  const matched = links.find((item) => {
    if (item.to === '/admin') {
      return route.path === '/admin'
    }
    return route.path.startsWith(item.to)
  })
  return matched || links[0]
})

const breadcrumbs = computed(() => {
  const crumbs = [{ label: '首页', to: '/' }]
  if (currentLink.value) {
    crumbs.push({ label: currentLink.value.label })
  }
  return crumbs
})

const currentTime = computed(() => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})
</script>

<template>
  <div class="admin-console">
    <!-- 左侧边栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <span class="logo-icon">🏫</span>
        </div>
        <div class="sidebar-brand-text">
          <h1>LOST</h1>
          <p>失物招领系统</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-section">
          <span class="nav-section-title">主导航</span>
          <RouterLink v-for="link in links" :key="link.to" :to="link.to" class="nav-item" :class="{ active: route.path.startsWith(link.to) }">
            <span class="nav-item-icon">{{ link.icon }}</span>
            <span class="nav-item-text">
              <strong>{{ link.label }}</strong>
              <small>{{ link.hint }}</small>
            </span>
          </RouterLink>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-time">
          <span class="time-icon">🕐</span>
          <span class="time-value">{{ currentTime }}</span>
        </div>
        <div class="sidebar-user">
          <div class="user-avatar">
            {{ userStore.displayName?.charAt(0).toUpperCase() || 'A' }}
          </div>
          <div class="user-info">
            <strong>{{ userStore.displayName || '管理员' }}</strong>
            <span>系统管理员</span>
          </div>
          <button class="user-logout" @click="userStore.logout(); $router.push('/login')">
            <span>🚪</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- 右侧工作区 -->
    <section class="admin-workspace">
      <!-- 顶部导航栏 -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <div class="topbar-breadcrumb">
            <span class="breadcrumb-home">🏠</span>
            <template v-for="(crumb, index) in breadcrumbs" :key="index">
              <span class="breadcrumb-sep">/</span>
              <RouterLink v-if="crumb.to" :to="crumb.to" class="breadcrumb-link">
                {{ crumb.label }}
              </RouterLink>
              <span v-else class="breadcrumb-current">{{ crumb.label }}</span>
            </template>
          </div>
        </div>

        <div class="topbar-center">
          <div class="topbar-search">
            <span class="search-icon">🔍</span>
            <input type="text" placeholder="搜索..." class="search-input" />
          </div>
        </div>

        <div class="topbar-right">
          <button class="topbar-action" title="通知">
            <span>🔔</span>
            <span class="action-badge">3</span>
          </button>
          <button class="topbar-action" title="消息">
            <span>💬</span>
          </button>
          <button class="topbar-action" title="设置">
            <span>⚙️</span>
          </button>
          <div class="topbar-user">
            <div class="topbar-avatar">
              {{ userStore.displayName?.charAt(0).toUpperCase() || 'A' }}
            </div>
          </div>
        </div>
      </header>

      <!-- 主内容区域 -->
      <main class="admin-content">
        <router-view />
      </main>
    </section>
  </div>
</template>