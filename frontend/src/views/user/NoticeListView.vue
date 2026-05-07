<script setup>
import { noticeApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const noticeState = useRemoteCollection(() => noticeApi.list(), [])
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Notice Feed</p>
        <h2>平台公告</h2>
      </div>
    </div>
    <p v-if="noticeState.error" class="feedback feedback-error">当前网络不可用，请刷新后重试。</p>
    <p v-else-if="noticeState.loading" class="feedback feedback-loading">加载中...</p>
    <p v-else-if="noticeState.items.length === 0" class="feedback feedback-empty">暂无公告</p>
    <div v-else class="panel-list">
      <RouterLink
        v-for="notice in noticeState.items"
        :key="notice.id"
        :to="{ name: 'notice-detail', params: { id: notice.id } }"
        class="notice-card notice-card-link"
      >
        <strong>{{ notice.title }}</strong>
        <p>{{ notice.summary || notice.content }}</p>
        <small>{{ notice.publishedAt }}</small>
      </RouterLink>
    </div>
  </section>
</template>

<style scoped>
.notice-card-link {
  display: block;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

.notice-card-link:hover {
  border-color: #6366f1;
  background: rgba(99, 102, 241, 0.04);
}
</style>
