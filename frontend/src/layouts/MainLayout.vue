<script setup>
import { computed, ref } from 'vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { useAuth } from '@/composables/useAuth'
import { useRoute } from 'vue-router'
import { categories } from '@/data/catalog'
import { categoriesApi } from '@/api/modules/categories'

const route = useRoute()
const searchKeyword = ref('')
const { userStore, isAuthenticated } = useAuth()
const categoriesState = useRemoteCollection(() => categoriesApi.list(), categories)

const links = [
  { label: '首页', to: '/' },
  { label: '失物大厅', to: '/lost' },
  { label: '招领大厅', to: '/found' },
  { label: '发布信息', to: '/publish/lost' },
  { label: '公告', to: '/notices' },
  { label: '认领进度', to: '/claims' },
]

const currentLink = computed(() => links.find((item) => item.to === route.path) || links[0])
const accountDisplayName = computed(
  () => userStore.displayName || userStore.profile?.realName || userStore.profile?.name || '同学',
)
const accountInitial = computed(() => accountDisplayName.value.slice(0, 1))
</script>

<template>
  <div class="portal-shell">
    <header class="portal-utility">
      <div class="portal-utility-inner">
        <p>您好，欢迎使用校园失物招领平台。</p>
        <nav class="portal-utility-nav">
          <RouterLink v-if="isAuthenticated" class="portal-account-link" to="/profile">
            <span class="portal-account-avatar" aria-hidden="true">{{ accountInitial }}</span>
            <span class="portal-account-copy">
              <strong>{{ accountDisplayName }}</strong>
              <span>查看个人中心</span>
            </span>
          </RouterLink>
          <template v-else>
            <RouterLink class="portal-auth-link" to="/login">登录</RouterLink>
            <RouterLink class="portal-auth-link is-accent" to="/register">注册</RouterLink>
          </template>
        </nav>
      </div>
    </header>

    <section class="portal-head">
      <div class="portal-brand">
        <div class="portal-brand-mark">寻</div>
        <div>
          <h1>寻物驿站</h1>
          <p>Campus Lost &amp; Found Marketplace</p>
        </div>
      </div>

      <div class="portal-search">
        <div class="portal-search-main">
          <input v-model="searchKeyword" type="text" placeholder="输入物品名称、地点或关键特征" />
          <button type="button">搜索</button>
        </div>
        <!-- <div class="portal-search-tags">
          <span v-for="item in categoriesState.items" :key="item">{{ item }}</span>
        </div> -->
      </div>

      <RouterLink class="portal-claim-cart" to="/claims">
        <!-- 点击判断是否登录，未登录提示请先登录 -->
        <span>我的认领单</span>
      </RouterLink>
    </section>

    <nav class="portal-nav">
      <RouterLink class="portal-nav-emphasis" to="/publish/lost">发布失物</RouterLink>
      <RouterLink v-for="link in links" :key="link.to" :to="link.to"
        :class="['portal-nav-link', { 'is-active': currentLink.to === link.to }]">
        {{ link.label }}
      </RouterLink>
    </nav>

    <main class="portal-main">
      <router-view />
    </main>
  </div>
</template>
