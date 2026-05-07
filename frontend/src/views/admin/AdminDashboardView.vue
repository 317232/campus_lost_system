<script setup>
import { computed } from 'vue'
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { dashboardStats } from '../../data/catalog'

const overviewState = useRemoteCollection(() => adminApi.getOverview(), dashboardStats)

const stats = computed(() =>
  overviewState.items.map((entry) => ({
    label: entry.label,
    value: entry.value,
  })),
)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Admin Overview</p>
        <h2>管理员总览</h2>
      </div>
    </div>
    <p v-if="overviewState.error" class="feedback feedback-error">统计接口不可用，当前显示为演示数据。</p>
    <div class="stats-grid">
      <article v-for="stat in stats" :key="stat.label" class="stat-card admin-card">
        <p>{{ stat.label }}</p>
        <strong>{{ stat.value }}</strong>
      </article>
    </div>
  </section>
</template>
