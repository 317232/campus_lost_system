<script setup>
import { ref } from 'vue'
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const reviewState = useRemoteCollection(() => adminApi.getReviewQueue(), [])

const feedbackMsg = ref('')
const feedbackType = ref('')

function showFeedback(msg, type = 'success') {
  feedbackMsg.value = msg
  feedbackType.value = type
  setTimeout(() => { feedbackMsg.value = '' }, 3000)
}

async function handleApprove(id) {
  try {
    await adminApi.auditItem(id, 'APPROVE')
    showFeedback('已通过审核')
    await reviewState.reload()
  } catch (e) {
    showFeedback(e?.message || '操作失败', 'error')
  }
}

async function handleReject(id) {
  const remark = prompt('请输入驳回原因（选填）：')
  try {
    await adminApi.auditItem(id, 'REJECT', remark || '')
    showFeedback('已驳回')
    await reviewState.reload()
  } catch (e) {
    showFeedback(e?.message || '操作失败', 'error')
  }
}
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Single-level Approval</p>
        <h2>待审核</h2>
      </div>
      <span v-if="feedbackMsg" :class="['global-feedback', feedbackType === 'error' ? 'feedback-error' : 'feedback-success']">
        {{ feedbackMsg }}
      </span>
      <button class="refresh-btn" @click="reviewState.reload()" :disabled="reviewState.loading.value">
        刷新
      </button>
    </div>

    <div v-if="reviewState.loading.value" class="feedback">加载中...</div>
    <div v-else-if="reviewState.error.value" class="feedback feedback-error">
      加载失败，请稍后重试
    </div>
    <div v-else-if="reviewState.items.length === 0" class="feedback">
      暂无待审核物品
    </div>
    <div v-else class="panel-list">
      <article v-for="item in reviewState.items" :key="item.id" class="review-card">
        <div class="card-topline">
          <span class="scene-tag">{{ item.scene === 'found' ? '招领' : '失物' }}</span>
          <span class="submitter">发布者ID: {{ item.ownerId || item.submitterId }}</span>
        </div>
        <strong class="item-title">{{ item.title || item.itemName }}</strong>
        <p class="item-desc">{{ item.description || item.itemName }} · {{ item.location }}</p>
        <div class="inline-actions">
          <button type="button" class="approve-btn" @click="handleApprove(item.id)">通过</button>
          <button type="button" class="reject-btn" @click="handleReject(item.id)">驳回</button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.global-feedback {
  font-size: 0.875rem;
  padding: 0.4rem 1rem;
  border-radius: 6px;
  width: 100%;
}

.eyebrow {
  margin: 0;
  font-size: 0.8rem;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

h2 {
  margin: 0.25rem 0 0;
  font-size: 1.4rem;
}

.refresh-btn {
  padding: 0.4rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 0.85rem;
  color: #475569;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.feedback {
  text-align: center;
  padding: 2rem;
  color: #64748b;
}

.feedback-error {
  color: #dc2626;
}

.panel-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.review-card {
  padding: 1rem 1.25rem;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.card-topline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #64748b;
}

.scene-tag {
  background: #eff6ff;
  color: #3b82f6;
  padding: 0.15rem 0.6rem;
  border-radius: 999px;
  font-weight: 600;
}

.item-title {
  font-size: 1rem;
  color: #0f172a;
}

.item-desc {
  margin: 0;
  font-size: 0.85rem;
  color: #64748b;
}

.inline-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.approve-btn {
  background: #16a34a;
  color: #fff;
  border: none;
  padding: 0.5rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
}

.approve-btn:hover {
  background: #15803d;
}

.reject-btn {
  background: #fff;
  color: #dc2626;
  border: 1px solid #fecaca;
  padding: 0.5rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
}

.reject-btn:hover {
  background: #fef2f2;
}
</style>
