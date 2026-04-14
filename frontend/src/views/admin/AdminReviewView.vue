<script setup>
import { computed } from 'vue'
import { adminApi } from '@/api/modules'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { reviewQueue } from '../../data/catalog'

const reviewState = useRemoteCollection(() => adminApi.getReviewQueue(), reviewQueue)

const displayQueue = computed(() =>
  reviewState.items.map((item) => ({
    ...item,
    owner: item.owner || item.submitter,
    stage: item.stage || item.queue,
    priority: item.priority || 'pending',
  })),
)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Single-level Approval</p>
        <h2>待审核</h2>
      </div>
    </div>
    <p v-if="reviewState.error" class="feedback feedback-error">审核功能不可用，请联系管理员。</p>
    <div class="panel-list">
      <article v-for="item in displayQueue" :key="item.id || item.code" class="review-card">
        <div class="card-topline">
          <StatusBadge :status="item.priority" />
          <span>{{ item.stage }}</span>
        </div>
        <strong>{{ item.title }}</strong>
        <p>提交人: {{ item.owner }}</p>
        <div class="inline-actions">
          <button type="button">通过</button>
          <button type="button" class="secondary">驳回</button>
        </div>
      </article>
    </div>
  </section>
</template>
