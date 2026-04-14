<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const links = [
  { label: '工作台', to: '/admin', hint: '总览与待办', icon: '台' },
  { label: '待审核', to: '/admin/reviews', hint: '失物 / 招领 / 认领', icon: '审' },
  { label: '用户管理', to: '/admin/users', hint: '账号与角色', icon: '户' },
  { label: '举报处理', to: '/admin/reports', hint: '反馈与处置', icon: '报' },
  { label: '公告管理', to: '/admin/notices', hint: '通知发布', icon: '告' },
  { label: '平台统计', to: '/admin/statistics', hint: '关键指标', icon: '统' },
]

const route = useRoute()

const currentLink = computed(() => {
  const matched = links.find((item) => {
    if (item.to === '/admin') {
      return route.path === '/admin'
    }

    return route.path.startsWith(item.to)
  })

  return matched || links[0]
})
</script>

<template>
  <div class="admin-console">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <p class="eyebrow">Campus Console</p>
        <h1>校园失物招领后台</h1>
        <span>左侧导航 · 右侧内容区</span>
      </div>

      <nav class="admin-side-nav">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="admin-nav-link"
        >
          <span class="admin-nav-icon">{{ link.icon }}</span>
          <span class="admin-nav-copy">
            <strong>{{ link.label }}</strong>
            <small>{{ link.hint }}</small>
          </span>
        </RouterLink>
      </nav>
    </aside>

    <section class="admin-workspace">
      <header class="admin-topbar">
        <div>
          <p class="eyebrow">Single-level Review</p>
          <h2>{{ currentLink.label }}</h2>
        </div>
        <div class="admin-topbar-meta">
          <span>JWT 鉴权</span>
          <span>课程演示后台</span>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </section>
  </div>
</template>
