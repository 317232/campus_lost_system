<script setup>
import { computed, onMounted, reactive } from 'vue'
import { mockMode } from '@/api'
import { adminApi } from '@/api/modules/admin'
import { dashboardStats as fallbackStats, trend as fallbackTrend } from '../../data/catalog'

const labelMap = {
  totalUsers: {
    label: '平台用户',
    hint: '普通用户与管理员总数',
  },
  totalLostPosts: {
    label: '失物发布总量',
    hint: '已录入的失物信息总数',
  },
  totalFoundPosts: {
    label: '招领发布总量',
    hint: '已录入的招领信息总数',
  },
  pendingLostReviews: {
    label: '待审核失物',
    hint: '发布后进入管理员工作台',
  },
  pendingFoundReviews: {
    label: '待审核招领',
    hint: '证件类优先人工确认',
  },
  pendingClaimReviews: {
    label: '待审核认领',
    hint: '需核对物品特征说明',
  },
  pendingReports: {
    label: '待处理举报',
    hint: '需管理员判定是否违规',
  },
  completedClaims: {
    label: '已完成认领',
    hint: '本周累计成功找回',
  },
}

const state = reactive({
  loading: false,
  error: '',
  overview: [],
  trend: [],
})

const stats = computed(() => {
  if (!state.overview.length) {
    return fallbackStats
  }

  return state.overview.map((item) => ({
    label: labelMap[item.label]?.label || item.label,
    value: item.value,
    hint: labelMap[item.label]?.hint || '课程版统计指标',
  }))
})

const trendEntries = computed(() => (state.trend.length ? state.trend : fallbackTrend))

async function loadStatistics() {
  if (mockMode) {
    state.overview = []
    state.trend = []
    return
  }

  state.loading = true
  state.error = ''

  try {
    const [overviewResponse, trendResponse] = await Promise.all([
      adminApi.getOverview(),
      adminApi.getTrend(),
    ])

    state.overview = overviewResponse || []
    state.trend = trendResponse || []
  } catch (error) {
    state.error = error instanceof Error ? error.message : '统计接口加载失败，已回退到演示数据。'
    state.overview = []
    state.trend = []
  } finally {
    state.loading = false
  }
}

onMounted(loadStatistics)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Minimal Metrics Set</p>
        <h2>平台数据统计</h2>
      </div>
    </div>

    <div class="stats-grid">
      <article v-for="stat in stats" :key="stat.label" class="stat-card admin-card">
        <p>{{ stat.label }}</p>
        <strong>{{ stat.value }}</strong>
        <span>{{ stat.hint }}</span>
      </article>
    </div>

    <p v-if="state.loading" class="feedback">正在加载真实统计数据...</p>
    <p v-else-if="state.error" class="feedback feedback-error">{{ state.error }}</p>

    <article class="panel trend-panel">
      <div class="panel-header">
        <div>
          <p class="eyebrow">7-Day Trend</p>
          <h3>近 7 日发布趋势</h3>
        </div>
      </div>
      <div class="trend-bars">
        <div v-for="entry in trendEntries" :key="entry.day" class="trend-bar">
          <span>{{ entry.day }}</span>
          <strong :style="{ height: `${entry.posts * 8}px` }"></strong>
          <small>{{ entry.posts }}</small>
        </div>
      </div>
    </article>
  </section>
</template>
