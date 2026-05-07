<script setup>
import { ref, computed } from 'vue'
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

// ─── Tab 切换 ─────────────────────────────────────────────────
const activeTab = ref('items') // 'items' | 'claims'

// ─── 物品审核 ─────────────────────────────────────────────────
const itemsState = useRemoteCollection(() => adminApi.getReviewQueue(), [])

// ─── 认领审核 ──────────────────────────────────────────────────
const claimsState = useRemoteCollection(() => adminApi.getClaimReviewQueue(), [], {
  select: (response) => {
    // adminApi.getClaimReviewQueue already unwraps axios layer,
    // so response is already the PageResponse: { records, total, page, pageSize }
    return response?.records || []
  }
})

// ─── 提示 ─────────────────────────────────────────────────────
const feedbackMsg = ref('')
const feedbackType = ref('')
function showFeedback(msg, type = 'success') {
  feedbackMsg.value = msg
  feedbackType.value = type
  setTimeout(() => { feedbackMsg.value = '' }, 3000)
}

// ─── 物品审核操作 ──────────────────────────────────────────────
async function handleItemApprove(id) {
  try {
    await adminApi.auditItem(id, 'APPROVE')
    showFeedback('物品已通过审核')
    await itemsState.reload()
  } catch (e) {
    showFeedback(e?.message || '操作失败', 'error')
  }
}

async function handleItemReject(id) {
  const remark = prompt('请输入驳回原因（选填）：')
  try {
    await adminApi.auditItem(id, 'REJECT', remark || '')
    showFeedback('物品已驳回')
    await itemsState.reload()
  } catch (e) {
    showFeedback(e?.message || '操作失败', 'error')
  }
}

// ─── 认领审核操作 ──────────────────────────────────────────────
async function handleClaimApprove(id) {
  try {
    await adminApi.auditClaim(id, 'APPROVE')
    showFeedback('认领已通过审核')
    await claimsState.reload()
  } catch (e) {
    showFeedback(e?.message || '操作失败', 'error')
  }
}

async function handleClaimReject(id) {
  const remark = prompt('请输入驳回原因（选填）：')
  try {
    await adminApi.auditClaim(id, 'REJECT', remark || '')
    showFeedback('认领已驳回')
    await claimsState.reload()
  } catch (e) {
    showFeedback(e?.message || '操作失败', 'error')
  }
}

// ─── 认领详情 ──────────────────────────────────────────────────
const detailModal = ref({ visible: false, loading: false, error: null, data: null })
async function openClaimDetail(claimId) {
  detailModal.value = { visible: true, loading: true, error: null, data: null }
  try {
    const res = await adminApi.getClaimAuditDetail(claimId)
    detailModal.value.data = res
    detailModal.value.loading = false
  } catch (e) {
    detailModal.value.error = e?.message || '加载详情失败'
    detailModal.value.loading = false
  }
}
function closeClaimDetail() {
  detailModal.value.visible = false
}

// ─── 当前列表状态 ───────────────────────────────────────────────
const currentState = computed(() => activeTab.value === 'items' ? itemsState : claimsState)
</script>

<template>
  <section class="page-section">
    <!-- 标题栏 -->
    <div class="panel-header">
      <div>
        <p class="eyebrow">Single-level Approval</p>
        <h2>待审核</h2>
      </div>
      <span v-if="feedbackMsg" :class="['global-feedback', feedbackType === 'error' ? 'feedback-error' : 'feedback-success']">
        {{ feedbackMsg }}
      </span>
      <button class="refresh-btn" @click="currentState.reload()" :disabled="currentState.loading.value">
        刷新
      </button>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <button :class="['tab-btn', activeTab === 'items' ? 'active' : '']" @click="activeTab = 'items'">物品审核</button>
      <button :class="['tab-btn', activeTab === 'claims' ? 'active' : '']" @click="activeTab = 'claims'">认领审核</button>
    </div>

    <!-- 物品列表 -->
    <template v-if="activeTab === 'items'">
      <div v-if="itemsState.loading.value" class="feedback">加载中...</div>
      <div v-else-if="itemsState.error.value" class="feedback feedback-error">加载失败，请稍后重试</div>
      <div v-else-if="itemsState.items.length === 0" class="feedback">暂无待审核物品</div>
      <div v-else class="panel-list">
        <article v-for="item in itemsState.items" :key="item.id" class="review-card">
          <div class="card-topline">
            <span class="scene-tag">{{ item.scene === 'found' ? '招领' : '失物' }}</span>
            <span class="submitter">发布者: {{ item.ownerName || item.ownerId }}</span>
          </div>
          <strong class="item-title">{{ item.title || item.itemName }}</strong>
          <p class="item-desc">{{ item.description || item.itemName }} · {{ item.location }}</p>
          <div class="inline-actions">
            <button type="button" class="approve-btn" @click="handleItemApprove(item.id)">通过</button>
            <button type="button" class="reject-btn" @click="handleItemReject(item.id)">驳回</button>
          </div>
        </article>
      </div>
    </template>

    <!-- 认领列表 -->
    <template v-else>
      <div v-if="claimsState.loading.value" class="feedback">加载中...</div>
      <div v-else-if="claimsState.error.value" class="feedback feedback-error">加载失败，请稍后重试</div>
      <div v-else-if="claimsState.items.length === 0" class="feedback">暂无待审核认领</div>
      <div v-else class="panel-list">
        <article v-for="claim in claimsState.items" :key="claim.id" class="review-card">
          <div class="card-topline">
            <span class="scene-tag claim-tag">认领</span>
            <span class="submitter">申请人: {{ claim.claimantName || claim.claimantId }}</span>
            <span class="student-no">学号: {{ claim.claimantStudentNo || '未知' }}</span>
          </div>
          <div class="claim-item-name">{{ claim.itemTitle || claim.itemName || '物品认领' }}</div>
          <p class="item-desc">
            <span class="field-label">认领说明:</span>
            {{ claim.claimDescription || '未填写' }}
          </p>
          <p class="item-desc">
            <span class="field-label">特征凭证:</span>
            {{ claim.proofMaterial || '未填写' }}
          </p>
          <p class="item-desc">
            <span class="field-label">联系方式:</span>
            {{ claim.contact || '未填写' }}
          </p>
          <p class="item-desc">
            <span class="field-label">状态:</span>
            <span :class="['status-badge', `status-${claim.status}`]">{{ claim.status || '未知' }}</span>
          </p>
          <p class="item-desc">
            <span class="field-label">提交时间:</span>
            {{ claim.createTime || '未知' }}
          </p>
          <div class="inline-actions">
            <button type="button" class="info-btn" @click="openClaimDetail(claim.id)">查看详情</button>
            <button type="button" class="approve-btn" @click="handleClaimApprove(claim.id)">通过</button>
            <button type="button" class="reject-btn" @click="handleClaimReject(claim.id)">驳回</button>
          </div>
        </article>
      </div>
    </template>

    <!-- 认领详情 Modal -->
    <div v-if="detailModal.visible" class="modal-overlay" @click.self="closeClaimDetail">
      <div class="modal-panel">
        <div class="modal-header">
          <h3>认领详情</h3>
          <button class="modal-close" @click="closeClaimDetail">×</button>
        </div>
        <div class="modal-body">
          <div v-if="detailModal.loading" class="feedback">加载中...</div>
          <div v-else-if="detailModal.error" class="feedback feedback-error">{{ detailModal.error }}</div>
          <template v-else-if="detailModal.data">
            <dl class="detail-list">
              <dt>业务ID</dt><dd>{{ detailModal.data.bizId ?? '—' }}</dd>
              <dt>物品ID</dt><dd>{{ detailModal.data.itemId ?? '—' }}</dd>
              <dt>物品名称</dt><dd>{{ detailModal.data.itemTitle || detailModal.data.itemName || '—' }}</dd>
              <dt>认领人</dt><dd>{{ detailModal.data.claimantName || detailModal.data.claimantId || '—' }}</dd>
              <dt>学号</dt><dd>{{ detailModal.data.claimantStudentNo || '—' }}</dd>
              <dt>认领说明</dt><dd>{{ detailModal.data.claimDescription || '—' }}</dd>
              <dt>凭证材料</dt><dd>{{ detailModal.data.proofMaterial || '—' }}</dd>
              <dt>联系方式</dt><dd>{{ detailModal.data.contact || '—' }}</dd>
              <dt>状态</dt>
              <dd><span :class="['status-badge', `status-${detailModal.data.status}`]">{{ detailModal.data.status || '—' }}</span></dd>
              <dt>创建时间</dt><dd>{{ detailModal.data.createTime || '—' }}</dd>
            </dl>
          </template>
          <div v-else class="feedback">暂无数据</div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.panel-header { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 1rem; }
.global-feedback { font-size: 0.875rem; padding: 0.4rem 1rem; border-radius: 6px; width: 100%; }
.eyebrow { margin: 0; font-size: 0.8rem; font-weight: 700; color: #2563eb; letter-spacing: 0.05em; text-transform: uppercase; }
h2 { margin: 0.25rem 0 0; font-size: 1.4rem; }
.refresh-btn { padding: 0.4rem 1rem; border: 1px solid #e2e8f0; border-radius: 6px; background: #fff; cursor: pointer; font-size: 0.85rem; color: #475569; }
.refresh-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.tab-bar { display: flex; gap: 0; margin-bottom: 1.5rem; border-bottom: 2px solid #e2e8f0; }
.tab-btn { padding: 0.5rem 1.25rem; border: none; background: none; cursor: pointer; font-size: 0.9rem; font-weight: 600; color: #64748b; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: color 0.15s, border-color 0.15s; }
.tab-btn:hover { color: #3b82f6; }
.tab-btn.active { color: #2563eb; border-bottom-color: #2563eb; }
.feedback { text-align: center; padding: 2rem; color: #64748b; }
.feedback-error { color: #dc2626; }
.panel-list { display: flex; flex-direction: column; gap: 1rem; }
.review-card { padding: 1rem 1.25rem; border: 1px solid #e2e8f0; border-radius: 10px; background: #fff; display: flex; flex-direction: column; gap: 0.5rem; }
.card-topline { display: flex; justify-content: space-between; align-items: center; font-size: 0.8rem; color: #64748b; }
.scene-tag { background: #eff6ff; color: #3b82f6; padding: 0.15rem 0.6rem; border-radius: 999px; font-weight: 600; }
.claim-tag { background: #f0fdf4; color: #16a34a; }
.item-title { font-size: 1rem; color: #0f172a; }
.item-desc { margin: 0; font-size: 0.85rem; color: #64748b; }
.inline-actions { display: flex; gap: 0.75rem; margin-top: 0.5rem; }
.approve-btn { background: #16a34a; color: #fff; border: none; padding: 0.5rem 1.25rem; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 0.875rem; }
.approve-btn:hover { background: #15803d; }
.reject-btn { background: #fff; color: #dc2626; border: 1px solid #fecaca; padding: 0.5rem 1.25rem; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 0.875rem; }
.reject-btn:hover { background: #fef2f2; }
.student-no { font-size: 0.75rem; color: #94a3b8; margin-left: 0.5rem; }
.claim-item-name { font-size: 1rem; color: #0f172a; font-weight: 600; margin: 0.25rem 0; }
.field-label { font-weight: 600; color: #475569; }
.status-badge { display: inline-block; padding: 0.1rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; }
.status-APPLIED { background: #eff6ff; color: #2563eb; }
.status-APPROVED { background: #f0fdf4; color: #16a34a; }
.status-REJECTED { background: #fef2f2; color: #dc2626; }
.status-CANCELLED { background: #f8fafc; color: #64748b; }
</style>
