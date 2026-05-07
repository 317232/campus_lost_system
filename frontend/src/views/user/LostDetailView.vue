<script setup>
import { ref, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { itemApi, userApi } from '@/api/modules'
import StatusBadge from '@/components/common/StatusBadge.vue'
import MatchRecommendations from '@/components/business/MatchRecommendations.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { isAuthenticated } = useAuth()

const detailState = useRemoteCollection(() => itemApi.getLostDetail(route.params.id), [], {
  select: (item) => item ? [item] : [],
})
const item = computed(() => detailState.items[0] ?? null)

// 当前用户信息（用于判断是否是物品主人）
const currentUser = ref(null)
const userLoading = ref(false)
async function loadCurrentUser() {
  userLoading.value = true
  try {
    currentUser.value = await userApi.getProfile()
  } catch (e) {
    currentUser.value = null
  } finally {
    userLoading.value = false
  }
}
loadCurrentUser()

// 是否是物品主人
const isOwner = computed(() => {
  if (!currentUser.value || !item.value) return false
  return currentUser.value.id === item.value.ownerId || currentUser.value.id === item.value.owner
})

// 可用的状态操作
const availableStatusActions = computed(() => {
  if (!item.value) return []
  const status = item.value.status
  const actions = []
  // LOST 物品主人可标记为已找到或下线
  if (item.value.type === 'lost' || item.value.scene === 'LOST') {
    if (status === 'PUBLISHED' || status === 'pending') {
      actions.push({ status: 'FOUND_BACK', label: '已找到', variant: 'primary' })
      actions.push({ status: 'OFFLINE', label: '下线物品', variant: 'secondary' })
    }
  }
  return actions
})

const contactUnlocked = ref(false)
const unlockingContact = ref(false)
const contactError = ref('')

// 状态更新相关
const statusUpdating = ref(false)
const statusFeedback = reactive({ type: '', message: '' })

async function updateItemStatus(targetStatus) {
  statusFeedback.type = ''
  statusFeedback.message = ''
  if (!item.value?.id || item.value.id > 1000000) {
    statusFeedback.type = 'error'
    statusFeedback.message = '当前是演示数据，不能执行此操作。'
    return
  }
  statusUpdating.value = true
  try {
    await itemApi.updateItemStatus(item.value.id, targetStatus)
    statusFeedback.type = 'success'
    statusFeedback.message = '状态已更新'
    await detailState.reload()
  } catch (e) {
    statusFeedback.type = 'error'
    statusFeedback.message = e?.message || '状态更新失败'
  } finally {
    statusUpdating.value = false
    setTimeout(() => { statusFeedback.message = '' }, 3000)
  }
}

async function unlockContact() {
  if (!item.value?.id || item.value.id > 1000000) return
  unlockingContact.value = true
  contactError.value = ''
  try {
    const resp = await itemApi.unlockContact(item.value.id, 'DETAIL_PAGE')
    item.value.contact = resp.contactValue || resp
    contactUnlocked.value = true
  } catch (e) {
    contactError.value = e?.message || '解锁失败'
  } finally {
    unlockingContact.value = false
  }
}

function goReport() {
  router.push({ name: 'report', query: { targetType: 'ITEM', targetId: route.params.id } })
}

function openFoundPost() {
  if (!isAuthenticated.value) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (!item.value?.id || item.value.id > 1000000) {
    console.warn('当前是演示数据，跳转发布页面仅供演示流程演示')
  }
  router.push({
    name: 'publish',
    params: { mode: 'found' },
    query: { relatedLostId: item.value.id, relatedLostTitle: item.value.title }
  })
}

</script>

<template>
  <!-- Loading state -->
  <div v-if="detailState.loading" class="state-container">
    <div class="loading-spinner"></div>
    <p>加载中...</p>
  </div>

  <!-- Error state -->
  <div v-else-if="detailState.error" class="state-container error">
    <p class="error-icon">!</p>
    <p>加载失败: {{ detailState.error }}</p>
    <button type="button" class="retry-btn" @click="detailState.reload()">重试</button>
  </div>

  <!-- Empty state -->
  <div v-else-if="!item" class="state-container empty">
    <p class="empty-icon">?</p>
    <p>未找到该物品详情</p>
  </div>

  <!-- Item detail -->
  <template v-else>
  <section class="detail-card">
    <div class="card-topline">
      <StatusBadge :status="item.status" />
      <span>{{ item.category }}</span>
      <span v-if="item.id > 1000000" class="demo-badge">演示数据</span>
    </div>
    <h2>{{ item.title }}</h2>
    <p class="section-copy">{{ item.description }}</p>
    <dl class="detail-grid">
      <div><dt>丢失地点</dt><dd>{{ item.location }}</dd></div>
      <div><dt>丢失时间</dt><dd>{{ item.time }}</dd></div>
      <div>
        <dt>联系方式</dt>
        <dd>
          <span v-if="item.id > 1000000">{{ item.contact }}</span>
          <template v-else>
            <template v-if="contactUnlocked">{{ item.contact }}</template>
            <button v-else type="button" class="unlock-btn" :disabled="unlockingContact" @click="unlockContact">
              {{ unlockingContact ? '解锁中...' : '点击解锁' }}
            </button>
          </template>
          <span v-if="contactError" class="error-text">{{ contactError }}</span>
        </dd>
      </div>
      <div><dt>发布人</dt><dd>{{ item.owner }}</dd></div>
    </dl>

    <!-- 物品主人状态操作 -->
    <div v-if="isOwner && availableStatusActions.length > 0" class="owner-actions">
      <p class="owner-label">物品管理</p>
      <div class="inline-actions">
        <button
          v-for="action in availableStatusActions"
          :key="action.status"
          type="button"
          :class="action.variant === 'primary' ? 'primary' : 'secondary'"
          :disabled="statusUpdating"
          @click="updateItemStatus(action.status)">
          {{ action.label }}
        </button>
      </div>
      <p v-if="statusFeedback.message" class="feedback" :class="`feedback-${statusFeedback.type}`">
        {{ statusFeedback.message }}
      </p>
    </div>

    <div class="inline-actions">
      <button type="button" class="claim-btn" @click="openFoundPost">我捡到了</button>
      <button type="button" class="secondary" @click="goReport">举报信息</button>
    </div>
  </section>

  <!-- 智能匹配推荐 -->
  <MatchRecommendations v-if="item.id" :itemId="item.id" scene="lost" />
  </template>
</template>

<style scoped>
.inline-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.owner-actions {
  margin-top: 1rem;
  padding: 0.9rem;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.06);
  border: 1px solid rgba(99, 102, 241, 0.15);
}

.owner-label {
  margin: 0 0 0.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: #6366f1;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

button.primary {
  background: #6366f1;
  color: #fff;
  border: none;
  padding: 0.5rem 1.1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

button.primary:hover:not(:disabled) {
  background: #4f46e5;
}

button.primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.unlock-btn {
  background: #10b981;
  color: #fff;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
}

.unlock-btn:hover:not(:disabled) {
  background: #059669;
}

.unlock-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.error-text {
  color: #ef4444;
  font-size: 0.75rem;
  margin-left: 0.5rem;
}

.feedback {
  margin: 0.75rem 0;
  padding: 0.65rem 0.8rem;
  border-radius: 12px;
  font-size: 0.88rem;
}

.feedback-success {
  color: #047857;
  background: rgba(16, 185, 129, 0.12);
}

.feedback-error {
  color: #b91c1c;
  background: rgba(239, 68, 68, 0.12);
}

/* 认领弹窗样式 */
.claim-modal {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.claim-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
}

.claim-dialog {
  position: relative;
  width: 90%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 1.75rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.claim-dialog h3 {
  margin: 0 0 0.35rem;
  font-size: 1.2rem;
  color: #0f172a;
}

.claim-hint {
  margin: 0 0 1.25rem;
  font-size: 0.85rem;
  color: #64748b;
}

.claim-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #334155;
  margin-bottom: 1rem;
}

.claim-field textarea,
.claim-field input {
  padding: 0.6rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  font-family: inherit;
}

.claim-field textarea:focus,
.claim-field input:focus {
  outline: none;
  border-color: #6366f1;
}

.claim-error {
  color: #dc2626;
  font-size: 0.85rem;
  margin: 0 0 1rem;
  padding: 0.5rem 0.75rem;
  background: rgba(239, 68, 68, 0.08);
  border-radius: 6px;
}

.claim-success {
  text-align: center;
  padding: 1.5rem 0;
  color: #16a34a;
  font-weight: 500;
}

.claim-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.25rem;
}

.cancel-btn {
  flex: 1;
  padding: 0.65rem;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
}

.submit-btn {
  flex: 2;
  padding: 0.65rem;
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
}

.submit-btn:hover:not(:disabled) {
  background: #4f46e5;
}

.submit-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

button.claim-btn {
  background: #6366f1;
  color: #fff;
  border: none;
  padding: 0.5rem 1.1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

button.claim-btn:hover {
  background: #4f46e5;
}

.demo-badge {
  display: inline-block;
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  background: #fef3c7;
  color: #92400e;
  font-size: 0.7rem;
  font-weight: 600;
  border: 1px solid #f59e0b;
}

.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 1rem;
  text-align: center;
  color: #64748b;
}

.state-container.error {
  color: #dc2626;
}

.state-container.empty {
  color: #94a3b8;
}

.loading-spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid #e2e8f0;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 0.75rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon, .empty-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.retry-btn {
  margin-top: 0.75rem;
  padding: 0.5rem 1rem;
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>
