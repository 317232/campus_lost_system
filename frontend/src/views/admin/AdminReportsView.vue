<script setup>
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { reportQueue } from '../../data/catalog'

const reportState = useRemoteCollection(() => adminApi.getReports(), reportQueue)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Report Queue</p>
        <h2>举报处理</h2>
      </div>
    </div>
    <p v-if="reportState.error" class="feedback feedback-error">举报功能不可用，请联系管理员。</p>
    <div class="panel-list">
      <article v-for="report in reportState.items" :key="report.id" class="notice-card">
        <strong>{{ report.title }}</strong>
        <p>{{ report.source }}</p>
        <small>{{ report.status }}</small>
      </article>
    </div>
  </section>
</template>
