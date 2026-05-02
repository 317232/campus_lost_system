<script setup>
import { computed, ref } from 'vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { useAuth } from '@/composables/useAuth'
import { useRoute, useRouter } from 'vue-router'
import { categories } from '@/data/catalog'
import { categoriesApi } from '@/api/modules/categories'

const route = useRoute()
const router = useRouter()
const searchKeyword = ref('')
const isMobileMenuOpen = ref(false)
const { userStore, isAuthenticated, logout } = useAuth()
const categoriesState = useRemoteCollection(() => categoriesApi.list(), categories)

// 子页面（详情页/表单页）需要显示顶部导航栏
const subPageNames = ['lost-detail', 'found-detail', 'publish', 'report', 'claims', 'profile']
const showSubNav = computed(() => subPageNames.includes(route.name))

// 上一页标题
const backPageName = computed(() => {
  const nameMap = {
    'lost-detail': '失物详情',
    'found-detail': '招领详情',
    'publish': '发布信息',
    'report': '举报',
    'claims': '认领进度',
    'profile': '个人中心',
  }
  return nameMap[route.name] || '返回'
})

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const links = [
  { label: '首页', to: '/' },
  { label: '失物大厅', to: '/lost' },
  { label: '招领大厅', to: '/found' },
  { label: '公告', to: '/notices' },
  { label: '认领进度', to: '/claims' },
]

const currentLink = computed(() => links.find((item) => item.to === route.path) || links[0])
const accountDisplayName = computed(
  () => userStore.displayName || userStore.profile?.realName || userStore.profile?.name || '同学',
)

const accountInitial = computed(() => accountDisplayName.value.slice(0, 1))

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/lost', query: { q: searchKeyword.value } })
  }
}
</script>

<template>
  <div class="portal-shell">
    <!-- 子页面顶部导航栏 -->
    <div v-if="showSubNav" class="sub-page-nav">
      <button class="back-btn" @click="goBack">
        <span class="back-arrow">‹</span>
        <span>{{ backPageName }}</span>
      </button>
      <span class="sub-page-title">{{ backPageName }}</span>
      <div class="sub-page-spacer"></div>
    </div>

    <!-- 头部区域 -->
    <header class="portal-header">
      <!-- 顶部欢迎栏 -->
      <div class="portal-welcome">
        <div class="welcome-inner">
          <p class="welcome-text">您好，欢迎使用校园失物招领平台</p>
          <nav class="welcome-nav">
            <template v-if="isAuthenticated">
              <RouterLink class="user-profile-link" to="/profile">
                <span class="user-avatar">{{ accountInitial }}</span>
                <span class="user-name">{{ accountDisplayName }}</span>
              </RouterLink>
              <span class="nav-divider">|</span>
              <button class="logout-btn" @click="logout(); router.push('/login')">退出</button>
            </template>
            <template v-else>
              <RouterLink class="auth-link" to="/login">登录</RouterLink>
              <span class="nav-divider">|</span>
              <RouterLink class="auth-link highlight" to="/register">注册</RouterLink>
            </template>
          </nav>
        </div>
      </div>

      <!-- Logo 和搜索区域 -->
      <section class="portal-brand-section">
        <div class="brand-inner">
          <RouterLink to="/" class="brand-logo">
            <div class="brand-icon">🏫</div>
            <div class="brand-text">
              <h1>寻物驿站</h1>
              <p>Campus Lost & Found</p>
            </div>
          </RouterLink>

          <div class="search-container">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索失物或招领信息..."
                @keyup.enter="handleSearch"
              />
              <button class="search-btn" @click="handleSearch">搜索</button>
            </div>
          </div>

          <div class="brand-actions">
            <RouterLink class="action-btn claim-btn" to="/claims">
              <span class="action-icon">📋</span>
              <!-- 点击认领显示请先登录的提示，同样点击发布信息也是 -->
              <span class="action-text">我的认领单</span>
            </RouterLink>
            <RouterLink class="action-btn publish-btn" to="/publish/lost">
              <span class="action-icon">➕</span>
              <span>发布信息</span>
            </RouterLink>
          </div>
        </div>
      </section>

      <!-- 主导航菜单
      <nav class="portal-nav">
        <div class="nav-inner">
          <div class="nav-left">
            <RouterLink
              v-for="link in links"
              :key="link.to"
              :to="link.to"
              class="nav-link"
              :class="{ 'is-active': currentLink.to === link.to }"
            >
              {{ link.label }}
            </RouterLink>
          </div>
          <div class="nav-right">
            <span class="nav-time">📅 {{ new Date().toLocaleDateString('zh-CN') }}</span>
          </div>
        </div>
      </nav> -->

    </header>

    <!-- 主内容区 -->
    <main class="portal-main">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="portal-footer">
      <div class="footer-inner">
        <p>© 2026 寻物驿站 · 校园失物招领平台</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ==================== 头部区域 ==================== */
.portal-header {
  background: linear-gradient(180deg, #fff 0%, #faf9f7 100%);
}

/* 欢迎栏 */
.portal-welcome {
  background: linear-gradient(90deg, #3d3d45 0%, #4a4a52 100%);
  padding: 0.6rem 0;
}

.welcome-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.welcome-text {
  margin: 0;
  font-size: 0.82rem;
  color: rgba(255, 255, 255, 0.8);
}

.welcome-nav {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-profile-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #fff;
  text-decoration: none;
}

.user-avatar {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #c85f34, #ef3f2f);
  color: #fff;
  font-size: 0.8rem;
  font-weight: 700;
}

.user-name {
  font-size: 0.85rem;
  color: #fff;
}

.nav-divider {
  color: rgba(255, 255, 255, 0.3);
  font-size: 0.8rem;
}

.auth-link {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.2s;
}

.auth-link:hover {
  color: #fff;
}

.auth-link.highlight {
  padding: 4px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #c85f34, #ef3f2f);
  color: #fff;
  font-weight: 600;
}

.logout-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  cursor: pointer;
  padding: 0;
}

.logout-btn:hover {
  color: #fff;
}

/* Logo和搜索区域 */
.portal-brand-section {
  padding: 1.5rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.brand-inner {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 2.5rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  text-decoration: none;
}

.brand-icon {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #c85f34, #ef3f2f);
  font-size: 1.5rem;
  box-shadow: 0 8px 24px rgba(200, 95, 52, 0.25);
}

.brand-text h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: 0.03em;
}

.brand-text p {
  margin: 2px 0 0;
  font-size: 0.72rem;
  color: #999;
  letter-spacing: 0.08em;
}

/* 搜索框 */
.search-container {
  max-width: 520px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 0.75rem;
  background: #fff;
  border: 2px solid rgba(200, 95, 52, 0.2);
  border-radius: 999px;
  transition: all 0.25s ease;
}

.search-box:focus-within {
  border-color: #c85f34;
  box-shadow: 0 4px 20px rgba(200, 95, 52, 0.12);
}

.search-icon {
  font-size: 1.1rem;
  opacity: 0.5;
  margin-left: 0.5rem;
}

.search-box input {
  flex: 1;
  border: none;
  background: none;
  font-size: 0.95rem;
  color: #333;
  outline: none;
}

.search-box input::placeholder {
  color: #aaa;
}

.search-btn {
  padding: 0.6rem 1.25rem;
  border-radius: 999px;
  background: linear-gradient(135deg, #c85f34, #ef3f2f);
  color: #fff;
  font-size: 0.88rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.search-btn:hover {
  opacity: 0.9;
  transform: scale(1.02);
}

/* 操作按钮 */
.brand-actions {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1.1rem;
  border-radius: 12px;
  font-size: 0.88rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
}

.claim-btn {
  background: #f5f0e8;
  color: #4f3c29;
  border: 1px solid rgba(79, 60, 41, 0.12);
}

.claim-btn:hover {
  background: #f0ebe3;
  border-color: rgba(79, 60, 41, 0.2);
}

.publish-btn {
  background: linear-gradient(135deg, #c85f34, #ef3f2f);
  color: #fff;
  box-shadow: 0 4px 16px rgba(200, 95, 52, 0.25);
}

.publish-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(200, 95, 52, 0.3);
}

.action-icon {
  font-size: 1rem;
}

/* 主导航 */
.portal-nav {
  background: #fff;
  border-bottom: 2px solid #f0ebe3;
}

.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.nav-left {
  display: flex;
  align-items: center;
}

.nav-link {
  padding: 1rem 1.25rem;
  font-size: 0.95rem;
  font-weight: 500;
  color: #666;
  text-decoration: none;
  position: relative;
  transition: all 0.2s;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #c85f34, #ef3f2f);
  border-radius: 3px 3px 0 0;
  transition: width 0.25s ease;
}

.nav-link:hover {
  color: #1a1a1a;
}

.nav-link:hover::after {
  width: 60%;
}

.nav-link.is-active {
  color: #c85f34;
  font-weight: 600;
}

.nav-link.is-active::after {
  width: 100%;
}

.nav-right {
  display: flex;
  align-items: center;
}

.nav-time {
  font-size: 0.82rem;
  color: #999;
}

/* 主内容区 */
.portal-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.5rem 1.5rem 2rem;
  min-height: 60vh;
}

/* 页脚 */
.portal-footer {
  background: #f5f0e8;
  border-top: 1px solid rgba(79, 60, 41, 0.1);
  padding: 1.5rem 0;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
  text-align: center;
}

.footer-inner p {
  margin: 0;
  font-size: 0.85rem;
  color: #999;
}

/* 子页面顶部导航栏 */
.sub-page-nav {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1.5rem;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  background: none;
  border: none;
  color: #1e2535;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(30, 37, 53, 0.06);
  color: #c85f34;
}

.back-arrow {
  font-size: 1.4rem;
  line-height: 1;
  font-weight: 400;
}

.sub-page-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1e2535;
  flex: 1;
  text-align: center;
  padding-right: 4rem;
}

.sub-page-spacer {
  width: 60px;
}

/* 响应式 */
@media (max-width: 900px) {
  .brand-inner {
    grid-template-columns: auto 1fr;
    gap: 1.5rem;
  }

  .brand-actions {
    display: none;
  }

  .search-container {
    grid-column: 1 / -1;
    max-width: 100%;
  }
}

@media (max-width: 600px) {
  .welcome-text {
    display: none;
  }

  .nav-inner {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .nav-link {
    padding: 0.85rem 1rem;
    font-size: 0.88rem;
  }
}
</style>