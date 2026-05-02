<script setup>
import { computed, onMounted, reactive } from 'vue'
import { mockMode } from '@/api'
import { adminApi } from '@/api/modules/admin'
import StatisticsChart from '@/components/admin/StatisticsChart.vue'

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

// 模拟数据
const fallbackChartData = {
  lostCount: 128,
  foundCount: 96,
  pendingLost: 8,
  pendingFound: 5,
  pendingClaim: 3,
  completedClaims: 26,
  rejectedClaims: 12,
  trendDays: ['04-06', '04-07', '04-08', '04-09', '04-10', '04-11', '04-12'],
  lostTrend: [4, 6, 5, 8, 6, 10, 7],
  foundTrend: [2, 3, 2, 4, 2, 4, 3],
}

const state = reactive({
  loading: false,
  error: '',
  overview: [],
  chartData: { ...fallbackChartData },
})

const stats = computed(() => {
  if (!state.overview.length) {
    return [
      { label: '待审核失物', value: 8, hint: '发布后 15 分钟内进入管理员工作台' },
      { label: '待审核招领', value: 5, hint: '证件类优先人工确认' },
      { label: '待审核认领', value: 3, hint: '需核对物品特征说明' },
      { label: '已完成认领', value: 26, hint: '本周累计成功找回' },
    ]
  }

  return state.overview.map((item) => ({
    label: labelMap[item.label]?.label || item.label,
    value: item.value,
    hint: labelMap[item.label]?.hint || '统计指标',
  }))
})

async function loadStatistics() {
  if (mockMode) {
    state.overview = []
    return
  }

  state.loading = true
  state.error = ''

  try {
    const overviewResponse = await adminApi.getOverview()
    state.overview = overviewResponse || []

    // 构建图表数据
    const overviewMap = {}
    ;(overviewResponse || []).forEach((item) => {
      overviewMap[item.label] = item.value
    })

    state.chartData = {
      lostCount: overviewMap.totalLostPosts || 0,
      foundCount: overviewMap.totalFoundPosts || 0,
      pendingLost: overviewMap.pendingLostReviews || 0,
      pendingFound: overviewMap.pendingFoundReviews || 0,
      pendingClaim: overviewMap.pendingClaimReviews || 0,
      completedClaims: overviewMap.completedClaims || 0,
      rejectedClaims: overviewMap.rejectedClaims || 0,
      trendDays: ['04-06', '04-07', '04-08', '04-09', '04-10', '04-11', '04-12'],
      lostTrend: [4, 6, 5, 8, 6, 10, 7],
      foundTrend: [2, 3, 2, 4, 2, 4, 3],
    }
  } catch (error) {
    state.error = error instanceof Error ? error.message : '统计接口加载失败，已回退到演示数据。'
    state.overview = []
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

    <StatisticsChart :data="state.chartData" />
  </section>
</template>

<style scoped>
.page-section {
  padding: 1.5rem;
}

.panel-header {
  margin-bottom: 1.5rem;
}

.panel-header h2 {
  margin: 0.25rem 0 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
}

.eyebrow {
  margin: 0;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #888;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.stat-card p {
  margin: 0 0 0.5rem;
  font-size: 0.875rem;
  color: #666;
}

.stat-card strong {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.25rem;
}

.stat-card span {
  font-size: 0.75rem;
  color: #999;
}

.feedback {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}

.feedback-error {
  background: #fef2f2;
  color: #dc2626;
}
</style>
