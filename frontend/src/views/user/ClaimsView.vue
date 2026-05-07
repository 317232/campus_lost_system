<script setup>
import { ref, computed, onMounted } from 'vue'
import { claimApi } from '@/api/modules'
import { useRequest } from '@/composables/useRequest'

// 直接使用 useRequest 手动控制加载和错误，不依赖 catalog fallback
const claimState = useRequest(() => claimApi.listMine())

// 重试函数
async function retryLoad() {
  await claimState.run()
}

// 认领状态中文映射
const statusLabel = {
  APPLIED: '待审核',
  CHECKING: '核对中',
  REVIEW_PENDING: '待最终审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  CANCELLED: '已取消',
}

const statusPriority = {
  REJECTED: 0,
  CANCELLED: 1,
  APPLIED: 2,
  CHECKING: 3,
  REVIEW_PENDING: 4,
  APPROVED: 5,
}

function getStatusLabel(status) {
  return statusLabel[status] || status || '未知'
}

function getStatusClass(status) {
  if (status === 'APPROVED') return 'badge-ok'
  if (status === 'REJECTED' || status === 'CANCELLED') return 'badge-err'
  if (status === 'APPLIED' || status === 'CHECKING' || status === 'REVIEW_PENDING') return 'badge-pending'
  return ''
}

// 进度阶段计算
function getProgressStage(status) {
  const stages = {
    APPLIED: 1,
    CHECKING: 2,
    REVIEW_PENDING: 3,
    APPROVED: 4,
    REJECTED: 0,
    CANCELLED: 0,
  }
  return stages[status] ?? 0
}

const progressLabels = ['已拒绝/取消', '提交申请', '管理员核对', '最终审核', '审核通过']

// 详情入口
function viewClaimDetail(claim) {
  // 目前路由层面无详情页，提示用户可查看进度
  console.info('查看认领详情', claim.id)
}
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Claim Tracking</p>
        <h2>认领进度</h2>
      </div>
    </div>

    <!-- Loading 状态 -->
    <div v-if="claimState.loading.value" class="claims-loading">
      <div class="loading-spinner"></div>
      <p>正在加载认领记录...</p>
    </div>

    <!-- Error 状态：API 失败，不展示 catalog 假数据 -->
    <div v-else-if="claimState.error.value" class="claims-error-state">
      <div class="error-icon">⚠️</div>
      <h3>无法加载认领记录</h3>
      <p>后端服务暂不可用，请检查网络或稍后重试。</p>
      <p class="error-detail">{{ claimState.error.value }}</p>
      <button class="retry-btn" @click="retryLoad">
        <span>🔄</span> 重试
      </button>
    </div>

    <!-- Empty 状态：无记录 -->
    <div v-else-if="!claimState.data.value || claimState.data.value.length === 0" class="claims-empty-state">
      <div class="empty-icon">📋</div>
      <h3>暂无认领记录</h3>
      <p>您还没有提交任何认领申请。</p>
      <p class="empty-hint">在物品详情页可以发起认领申请。</p>
    </div>

    <!-- 认领列表 -->
    <div v-else class="panel-list claims-list">
      <article
        v-for="claim in claimState.data.value"
        :key="claim.id"
        class="claim-card"
        @click="viewClaimDetail(claim)"
      >
        <!-- 进度指示器 -->
        <div class="claim-progress" v-if="claim.status !== 'REJECTED' && claim.status !== 'CANCELLED'">
          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{ width: (getProgressStage(claim.status) / 4 * 100) + '%' }"
            ></div>
          </div>
          <div class="progress-steps">
            <span
              v-for="(label, idx) in progressLabels.slice(1)"
              :key="idx"
              class="progress-step"
              :class="{
                'step-done': getProgressStage(claim.status) > idx + 1,
                'step-active': getProgressStage(claim.status) === idx + 1
              }"
            >{{ label }}</span>
          </div>
        </div>

        <div class="claim-header">
          <strong>{{ claim.itemName || claim.itemTitle || '物品 #' + claim.itemId }}</strong>
          <span :class="['audit-badge', getStatusClass(claim.status)]">
            {{ getStatusLabel(claim.status) }}
          </span>
        </div>

        <p class="claim-proof">
          <span class="field-label">证明材料:</span>
          {{ claim.proofDescription || claim.proofMaterial || claim.proof || '—' }}
        </p>
        <p class="claim-contact">
          <span class="field-label">联系方式:</span>
          {{ claim.contact || '—' }}
        </p>
        <p v-if="claim.adminComment" class="claim-admin-comment">
          <span class="field-label">审核备注:</span>
          {{ claim.adminComment }}
        </p>

        <div class="claim-footer">
          <small class="claim-time">{{ claim.createTime || claim.updatedAt || '' }}</small>
          <span class="detail-link" v-if="claim.status">查看详情 ›</span>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.panel-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
.eyebrow { margin: 0; font-size: 0.8rem; font-weight: 700; color: #c85f34; letter-spacing: 0.05em; text-transform: uppercase; }
h2 { margin: 0.25rem 0 0; font-size: 1.4rem; color: #1a1a1a; }

/* Loading 状态 */
.claims-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem 1rem;
  gap: 0.75rem;
  color: #64748b;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e2e8f0;
  border-top-color: #c85f34;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error 状态 */
.claims-error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 2.5rem 1.5rem;
  background: #fff;
  border: 1px solid #fee2e2;
  border-radius: 16px;
}

.error-icon { font-size: 2.5rem; margin-bottom: 0.5rem; }

.claims-error-state h3 {
  margin: 0 0 0.5rem;
  color: #991b1b;
  font-size: 1.1rem;
}

.claims-error-state p {
  margin: 0 0 0.4rem;
  color: #64748b;
  font-size: 0.9rem;
}

.error-detail {
  font-size: 0.8rem !important;
  color: #94a3b8 !important;
  font-family: monospace;
}

.retry-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin-top: 1rem;
  padding: 0.6rem 1.4rem;
  background: linear-gradient(135deg, #c85f34, #ef3f2f);
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.retry-btn:hover { opacity: 0.9; }

/* Empty 状态 */
.claims-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 3rem 1.5rem;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
}

.empty-icon { font-size: 2.5rem; margin-bottom: 0.5rem; }

.claims-empty-state h3 {
  margin: 0 0 0.5rem;
  color: #334155;
  font-size: 1.05rem;
}

.claims-empty-state p {
  margin: 0 0 0.4rem;
  color: #64748b;
  font-size: 0.88rem;
}

.empty-hint {
  color: #94a3b8 !important;
  font-size: 0.82rem !important;
}

/* 列表 */
.panel-list { display: flex; flex-direction: column; gap: 1rem; }

.claim-card {
  padding: 1.1rem 1.25rem;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.claim-card:hover {
  border-color: rgba(200, 95, 52, 0.3);
  box-shadow: 0 4px 16px rgba(200, 95, 52, 0.08);
}

/* 进度条 */
.claim-progress {
  margin-bottom: 0.5rem;
}

.progress-track {
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.4rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #c85f34, #ef3f2f);
  border-radius: 2px;
  transition: width 0.4s ease;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
}

.progress-step {
  font-size: 0.68rem;
  color: #94a3b8;
}

.progress-step.step-active {
  color: #c85f34;
  font-weight: 600;
}

.progress-step.step-done {
  color: #64748b;
}

/* 卡片头部 */
.claim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.claim-header strong {
  font-size: 1rem;
  color: #0f172a;
}

/* 字段 */
.claim-proof,
.claim-contact,
.claim-admin-comment {
  margin: 0;
  font-size: 0.85rem;
  color: #475569;
}

.field-label {
  color: #94a3b8;
  font-size: 0.78rem;
  margin-right: 0.3rem;
}

.claim-admin-comment {
  padding: 0.5rem 0.65rem;
  background: rgba(99, 102, 241, 0.06);
  border-radius: 6px;
  font-size: 0.82rem;
}

/* 卡片底部 */
.claim-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.25rem;
}

.claim-time {
  font-size: 0.775rem;
  color: #94a3b8;
}

.detail-link {
  font-size: 0.82rem;
  color: #c85f34;
  font-weight: 500;
}

/* 状态徽章 */
.audit-badge {
  font-size: .75rem;
  font-weight: 600;
  border-radius: 999px;
  padding: .2rem .65rem;
  white-space: nowrap;
  flex-shrink: 0;
}

.badge-ok {
  background: rgba(34,197,94,.12);
  border: 1px solid rgba(34,197,94,.25);
  color: #16a34a;
}

.badge-pending {
  background: rgba(245,158,11,.12);
  border: 1px solid rgba(245,158,11,.25);
  color: #b45309;
}

.badge-err {
  background: rgba(239,68,68,.12);
  border: 1px solid rgba(239,68,68,.25);
  color: #dc2626;
}
</style>
