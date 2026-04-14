<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { categoriesApi } from '@/api/modules'
import { useAuth } from '@/composables/useAuth'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { categories } from '../../data/catalog'

const route = useRoute()
const { isAuthenticated } = useAuth()

const isFoundMode = computed(() => route.params.mode === 'found')
const title = computed(() => (isFoundMode.value ? '发布招领信息' : '发布失物信息'))
const eyebrow = computed(() => (isFoundMode.value ? 'Found Item Form' : 'Lost Item Form'))
const guestPrompt = computed(() => (isFoundMode.value ? '登录后即可发布招领线索。' : '登录后即可发布失物信息。'))
const categoriesState = useRemoteCollection(
  () => (isAuthenticated.value ? categoriesApi.list() : Promise.resolve(categories)),
  categories,
)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
        <p v-if="!isAuthenticated" class="section-copy">游客可先查看发布入口，但提交前需要完成登录。</p>
      </div>
    </div>

    <form v-if="isAuthenticated" class="publish-grid">
      <label>物品名称<input placeholder="请输入物品名称" /></label>
      <label>
        物品分类
        <select>
          <option v-for="category in categoriesState.items" :key="category">{{ category }}</option>
        </select>
      </label>
      <label>地点<input placeholder="请输入地点" /></label>
      <label>时间<input type="datetime-local" /></label>
      <label class="full-span">物品描述<textarea rows="5" placeholder="补充特征说明"></textarea></label>
      <label>联系方式<input placeholder="手机号 / QQ / 微信" /></label>
      <label>图片路径<input placeholder="/uploads/2026/demo-item.jpg" /></label>
      <div class="full-span inline-actions">
        <button type="button">保存草稿</button>
        <button type="button" class="secondary">提交审核</button>
      </div>
    </form>

    <div v-else class="publish-guest-card">
      <p class="publish-guest-kicker">请先登录</p>
      <h3>当前仅开放浏览，暂不支持游客直接发布</h3>
      <p class="section-copy">{{ guestPrompt }}</p>
      <div class="publish-guest-actions">
        <RouterLink :to="{ name: 'login', query: { redirect: route.fullPath } }">去登录</RouterLink>
        <RouterLink :to="{ name: 'register', query: { redirect: route.fullPath } }">注册账号</RouterLink>
        <RouterLink :to="{ name: 'home' }">返回首页</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.publish-guest-card {
  display: grid;
  gap: 1rem;
  padding: 1.5rem;
  border: 1px dashed rgba(59, 130, 246, 0.35);
  border-radius: 1.25rem;
  background: rgba(255, 255, 255, 0.9);
}

.publish-guest-card h3 {
  margin: 0;
  font-size: 1.125rem;
  color: #0f172a;
}

.publish-guest-kicker {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 700;
  color: #2563eb;
}

.publish-guest-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.publish-guest-actions a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.7rem 1rem;
  border-radius: 999px;
  border: 1px solid rgba(37, 99, 235, 0.2);
  color: #1d4ed8;
  background: rgba(239, 246, 255, 0.95);
  font-weight: 600;
  text-decoration: none;
}
</style>
