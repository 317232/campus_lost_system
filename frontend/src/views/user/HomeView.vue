<script setup>
import { computed } from 'vue'
import { itemApi, noticeApi } from '@/api/modules'
import ItemCard from '@/components/business/ItemCard.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { dashboardStats, notices } from '../../data/catalog'

const noticeState = useRemoteCollection(() => noticeApi.list(), notices)
const lostState = useRemoteCollection(() => itemApi.getLostItems(), [], {
  select: (response) => response?.records || [],
})
const foundState = useRemoteCollection(() => itemApi.getFoundItems(), [], {
  select: (response) => response?.records || [],
})

const displayNotices = computed(() => noticeState.items)
const featuredLost = computed(() =>
  lostState.items.map((item) => ({
    ...item,
    type: 'lost',
    status: item.stage?.toLowerCase?.() === 'published' ? 'urgent' : item.status || 'pending',
  })),
)
const featuredFound = computed(() =>
  foundState.items.map((item) => ({
    ...item,
    type: 'found',
    status: item.stage?.toLowerCase?.() === 'claimed' ? 'claimed' : item.status || 'pending',
  })),
)
</script>

<template>
  <section class="market-hero">
    <div class="market-hero-copy">
      <p class="eyebrow">Campus Portal</p>
      <h2>校园失物，一站查找与认领</h2>
      <p class="section-copy">
        集失物、招领、认领和公告的校园生活门户。
        先搜索，再发布，更快找回失物
      </p>
      <div class="hero-actions">
        <RouterLink to="/lost">进入失物大厅</RouterLink>
        <RouterLink to="/found">浏览招领信息</RouterLink>
      </div>
    </div>

    <div class="market-hero-visual">
      <div class="hero-glow hero-glow-left"></div>
      <div class="hero-glow hero-glow-right"></div>
      <article class="hero-highlight-card">
        <span>失物发布</span>
        <strong>快速曝光，让失物回到主人手中</strong>
      </article>
      <article class="hero-highlight-card value">
        <span>认领流程</span>
        <strong>状态更新清晰可追踪，更快找回失物</strong>
      </article>
    </div>
  </section>

  <section class="market-metrics">
    <!-- <div class="market-section-title">
      <div class="section-heading">
        <h3>平台速览</h3>
      </div>
    </div> -->
    <div class="stats-grid">
      <article v-for="stat in dashboardStats" :key="stat.label" class="stat-card" :data-label="stat.label">
        <p>{{ stat.label }}</p>
        <strong>{{ stat.value }}</strong>
        <span>{{ stat.hint }}</span>
      </article>
    </div>
    <p class="demo-badge">演示统计 / 非实时后端数据</p>
  </section>

  <section class="market-banner">
    <div>
      <strong>校园值班提醒</strong>
      <span>工作日 9:00 - 17:30 可携带有效证明前往值班室认领</span>
    </div>
    <RouterLink to="/notices">查看公告</RouterLink>
  </section>

  <section class="market-section">
    <div class="market-section-title">
      <h3>近期失物</h3>
      <RouterLink to="/lost">查看全部</RouterLink>
    </div>
    <p v-if="lostState.error" class="feedback feedback-error">失物加载失败，请刷新后重试。</p>
    <p v-else-if="lostState.loading" class="feedback feedback-loading">加载中...</p>
    <p v-else-if="featuredLost.length === 0" class="feedback feedback-empty">暂无失物信息</p>
    <div v-else class="market-card-grid">
      <ItemCard v-for="item in featuredLost" :key="item.id" :item="item" />
    </div>
  </section>

  <section class="market-section">
    <div class="market-section-title">
      <h3>招领信息</h3>
      <RouterLink to="/found">查看全部</RouterLink>
    </div>
    <p v-if="foundState.error" class="feedback feedback-error">招领信息加载失败，请刷新后重试。</p>
    <p v-else-if="foundState.loading" class="feedback feedback-loading">加载中...</p>
    <p v-else-if="featuredFound.length === 0" class="feedback feedback-empty">暂无招领信息</p>
    <div v-else class="market-card-grid">
      <ItemCard v-for="item in featuredFound" :key="item.id" :item="item" />
    </div>
  </section>

  <section class="market-section market-notice-section">
    <div class="market-section-title">
      <h3>平台公告</h3>
      <RouterLink to="/notices">查看全部</RouterLink>
    </div>
    <div v-if="noticeState.loading" class="feedback feedback-loading">加载中...</div>
    <p v-else-if="noticeState.error" class="feedback feedback-error">公告加载失败：{{ noticeState.error }}，已回退到演示数据。</p>
    <div v-else class="market-notice-grid">
      <article v-for="notice in displayNotices" :key="notice.id" class="notice-card">
        <strong>{{ notice.title }}</strong>
        <p>{{ notice.summary || notice.content }}</p>
        <small>{{ notice.publishedAt }}</small>
      </article>
    </div>
  </section>

  <section class="market-service-strip">
    <article>
      <strong>正</strong>
      <span>实名信息可追溯</span>
    </article>
    <article>
      <strong>审</strong>
      <span>管理员单级审核</span>
    </article>
    <article>
      <strong>认</strong>
      <span>认领流程在线留痕</span>
    </article>
    <article>
      <strong>达</strong>
      <span>线索与状态同步更新</span>
    </article>
  </section>
</template>

<style scoped>
.demo-badge {
  text-align: center;
  margin-top: 0.75rem;
  font-size: 0.75rem;
  color: #d97706;
  background: #fffbeb;
  border: 1px solid #fcd34d;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  display: inline-block;
}
</style>