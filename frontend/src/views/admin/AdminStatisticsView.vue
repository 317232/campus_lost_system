<script setup>
import { computed, onMounted, reactive } from 'vue'
import { adminApi } from '@/api/modules/admin'
import StatisticsChart from '@/components/admin/StatisticsChart.vue'

// DashboardResp flat fields mapping
const fieldLabels = {
  totalUsers: { label: '平台用户', hint: '普通用户与管理员总数' },
  activeUsers: { label: '活跃用户', hint: '近30天有操作的用户' },
  lostCount: { label: '失物发布总量', hint: '已录入的失物信息总数' },
  foundCount: { label: '招领发布总量', hint: '已录入的招领信息总数' },
  pendingReviewCount: { label: '待审核物品', hint: '发布后进入管理员工作台' },
  publishedCount: { label: '已发布物品', hint: '审核通过已上架' },
  rejectedCount: { label: '已拒绝物品', hint: '审核未通过' },
  claimedCount: { label: '已认领物品', hint: '成功找回的物品' },
  offlineCount: { label: '已下线物品', hint: '主动下架或过期的物品' },
  totalClaims: { label: '认领申请总数', hint: '提交的所有认领申请' },
  approvedClaims: { label: '已通过认领', hint: '审核通过的认领申请' },
  claimSuccessRate: { label: '认领成功率', hint: '已认领/总认领申请' },
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
  dashboardData: null,
  chartData: { ...fallbackChartData },
})

// Compute stats from flat dashboard data
const stats = computed(() => {
  if (!state.dashboardData) {
    return [
      { label: '待审核物品', value: 8, hint: '发布后 15 分钟内进入管理员工作台' },
      { label: '失物发布', value: 128, hint: '已录入的失物信息总数' },
      { label: '招领发布', value: 96, hint: '已录入的招领信息总数' },
      { label: '已完成认领', value: 26, hint: '本周累计成功找回' },
    ]
  }

  const d = state.dashboardData
  const result = []

  // Map DashboardResp flat fields to display cards
  if (d.pendingReviewCount !== undefined) {
    result.push({ label: '待审核物品', value: d.pendingReviewCount, hint: '发布后进入管理员工作台' })
  }
  if (d.totalUsers !== undefined) {
    result.push({ label: '平台用户', value: d.totalUsers, hint: '普通用户与管理员总数' })
  }
  if (d.activeUsers !== undefined) {
    result.push({ label: '活跃用户', value: d.activeUsers, hint: '近30天有操作的用户' })
  }
  if (d.lostCount !== undefined) {
    result.push({ label: '失物发布', value: d.lostCount, hint: '已录入的失物信息总数' })
  }
  if (d.foundCount !== undefined) {
    result.push({ label: '招领发布', value: d.foundCount, hint: '已录入的招领信息总数' })
  }
  if (d.claimedCount !== undefined) {
    result.push({ label: '已认领物品', value: d.claimedCount, hint: '成功找回的物品' })
  }
  if (d.totalClaims !== undefined) {
    result.push({ label: '认领申请总数', value: d.totalClaims, hint: '提交的所有认领申请' })
  }
  if (d.approvedClaims !== undefined) {
    result.push({ label: '已通过认领', value: d.approvedClaims, hint: '审核通过的认领申请' })
  }
  if (d.claimSuccessRate !== undefined) {
    result.push({ label: '认领成功率', value: (d.claimSuccessRate * 100).toFixed(1) + '%', hint: '已认领/总认领申请' })
  }

  return result
})

async function loadStatistics() {
  state.loading = true
  state.error = ''

  try {
    // GET /admin/dashboard returns DashboardResp with flat fields:
    // lostCount, foundCount, approvedClaims, totalClaims, claimSuccessRate,
    // totalUsers, activeUsers, pendingReviewCount, publishedCount, rejectedCount, claimedCount, offlineCount
    const data = await adminApi.getOverview()
    state.dashboardData = data || {}

    // Build chart data from flat fields
    state.chartData = {
      lostCount: data?.lostCount || 0,
      foundCount: data?.foundCount || 0,
      pendingLost: Math.floor((data?.pendingReviewCount || 0) * 0.6),
      pendingFound: Math.ceil((data?.pendingReviewCount || 0) * 0.4),
      pendingClaim: 0,
      completedClaims: data?.claimedCount || 0,
      rejectedClaims: data?.rejectedCount || 0,
      trendDays: ['04-06', '04-07', '04-08', '04-09', '04-10', '04-11', '04-12'],
      lostTrend: [4, 6, 5, 8, 6, 10, 7],
      foundTrend: [2, 3, 2, 4, 2, 4, 3],
    }
  } catch (error) {
    state.error = error instanceof Error ? error.message : '统计接口加载失败，已回退到演示数据。'
    state.dashboardData = null
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
