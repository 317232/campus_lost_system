<script setup>
import { ref, computed, reactive } from 'vue'
import { itemApi, claimApi, userApi } from '@/api/modules'
import { useRoute, useRouter } from 'vue-router'
import StatusBadge from '@/components/common/StatusBadge.vue'
import MatchRecommendations from '@/components/business/MatchRecommendations.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { isAuthenticated } = useAuth()
const detailState = useRemoteCollection(() => itemApi.getFoundDetail(route.params.id), [], {
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
  // FOUND 物品主人可标记为已认领或下线
  if (item.value.type === 'found' || item.value.scene === 'FOUND') {
    if (status === 'PUBLISHED' || status === 'pending') {
      actions.push({ status: 'CLAIMED', label: '标记已认领', variant: 'primary' })
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

const claimDialogVisible = ref(false)
const claimSubmitting = ref(false)
const claimFeedback = reactive({ type: '', message: '' })
const claimForm = reactive({
  claimDescription: '',
  proofMaterial: '',
  contact: '',
})

async function unlockContact() {
  if (!item.value?.id || item.value.id > 1000000) return
  unlockingContact.value = true
  contactError.value = ''
  try {
    const resp = await itemApi.unlockContact(item.value.id, 'DETAIL_PAGE')
    item.value.contact = resp.contactValue || resp
    if (!claimForm.contact) {
      claimForm.contact = item.value.contact || ''
    }
    contactUnlocked.value = true
  } catch (e) {
    contactError.value = e?.message || '解锁失败'
  } finally {
    unlockingContact.value = false
  }
}

function openClaimDialog() {
  if (!isAuthenticated.value) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  claimFeedback.type = ''
  claimFeedback.message = ''
  if (!item.value?.id) {
    claimFeedback.type = 'error'
    claimFeedback.message = '物品信息尚未加载完成，请稍后再试。'
    return
  }
  if (item.value.id > 1000000) {
    claimFeedback.type = 'error'
    claimFeedback.message = '当前是演示数据，不能提交真实认领申请。请先连接后端真实数据。'
    return
  }
  claimDialogVisible.value = true
}

function closeClaimDialog() {
  if (claimSubmitting.value) return
  claimDialogVisible.value = false
}

async function submitClaim() {
  claimFeedback.type = ''
  claimFeedback.message = ''

  if (!claimForm.claimDescription.trim()) {
    claimFeedback.type = 'error'
    claimFeedback.message = '请填写认领说明。'
    return
  }
  if (!claimForm.proofMaterial.trim()) {
    claimFeedback.type = 'error'
    claimFeedback.message = '请填写物品特征证明，方便管理员核对。'
    return
  }
  if (!claimForm.contact.trim()) {
    claimFeedback.type = 'error'
    claimFeedback.message = '请填写联系方式。'
    return
  }

  claimSubmitting.value = true
  try {
    await claimApi.applyClaim({
      itemId: item.value.id,
      claimDescription: claimForm.claimDescription.trim(),
      proofMaterial: claimForm.proofMaterial.trim(),
      contact: claimForm.contact.trim(),
    })
    claimFeedback.type = 'success'
    claimFeedback.message = '认领申请已提交，请等待管理员审核。'
    claimForm.claimDescription = ''
    claimForm.proofMaterial = ''
    claimForm.contact = ''
    window.setTimeout(() => {
      claimDialogVisible.value = false
      router.push({ name: 'claims' })
    }, 700)
  } catch (e) {
    claimFeedback.type = 'error'
    claimFeedback.message = e?.message || '认领申请提交失败，请稍后重试。'
  } finally {
    claimSubmitting.value = false
  }
}

function goReport() {
  router.push({ name: 'report', query: { targetType: 'ITEM', targetId: route.params.id } })
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
    <p v-if="claimFeedback.message && !claimDialogVisible" class="feedback" :class="`feedback-${claimFeedback.type}`">
      {{ claimFeedback.message }}
    </p>

    <div class="card-topline">
      <StatusBadge :status="item.status" />
      <span>{{ item.category || item.categoryName }}</span>
      <span v-if="item.id > 1000000" class="demo-badge">演示数据</span>
    </div>
    <h2>{{ item.title || item.itemName }}</h2>
    <p class="section-copy">描述: {{ item.description }}</p>

    <dl class="detail-grid">
      <div class="detail-item">
        <div class="detail-item-box" v-if="item.image">
          <img :src="item.image" alt="物品图片" />
        </div>

        <div class="detail-item-content">
          <dt>拾取地点</dt>
          <dd>{{ item.location }}</dd>
          <dt>拾取时间</dt>
          <dd>{{ item.time || item.timeLabel }}</dd>
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
          <dt>发布人</dt>
          <dd>{{ item.owner || item.ownerName }}</dd>
        </div>
      </div>
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
      <button type="button" @click="openClaimDialog">发起认领</button>
      <button type="button" class="secondary" @click="goReport">举报信息</button>
    </div>
  </section>
  
  <div v-if="claimDialogVisible" class="claim-modal">
    <div class="claim-overlay" @click="closeClaimDialog"></div>
    <form class="claim-dialog" @submit.prevent="submitClaim">
      <div class="claim-dialog-header">
        <div>
          <p class="eyebrow">Claim Application</p>
          <h3>发起认领申请</h3>
        </div>
        <button type="button" class="claim-close" :disabled="claimSubmitting" @click="closeClaimDialog">×</button>
      </div>

      <div class="claim-target">
        <span>认领物品</span>
        <strong>{{ item.title || item.itemName }}</strong>
        <small>{{ item.location }} · {{ item.time || item.timeLabel }}</small>
      </div>

      <label class="claim-field">
        <span>认领说明</span>
        <textarea v-model="claimForm.claimDescription" rows="3" placeholder="说明为什么确认这是你的物品，例如丢失时间、地点、使用痕迹等。"></textarea>
      </label>

      <label class="claim-field">
        <span>物品特征证明</span>
        <textarea v-model="claimForm.proofMaterial" rows="3" placeholder="填写只有失主知道的特征，例如贴纸、划痕、卡号后四位、内部物品等。"></textarea>
      </label>

      <label class="claim-field">
        <span>联系方式</span>
        <input v-model="claimForm.contact" placeholder="手机号 / 邮箱 / 微信等" />
      </label>

      <p v-if="claimFeedback.message" class="feedback" :class="`feedback-${claimFeedback.type}`">
        {{ claimFeedback.message }}
      </p>

      <div class="claim-actions">
        <button type="button" class="secondary" :disabled="claimSubmitting" @click="closeClaimDialog">取消</button>
        <button type="submit" :disabled="claimSubmitting">
          {{ claimSubmitting ? '提交中...' : '提交认领申请' }}
        </button>
      </div>
    </form>
  </div>

  <!-- 智能匹配推荐 -->
  <MatchRecommendations v-if="item.id" :itemId="item.id" scene="found" />
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

.claim-modal {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: grid;
  place-items: center;
  padding: 1rem;
}

.claim-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(6px);
}

.claim-dialog {
  position: relative;
  width: min(560px, 100%);
  border: 1px solid rgba(16, 185, 129, 0.22);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28);
  padding: 1.4rem;
}

.claim-dialog-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.claim-dialog-header h3 {
  margin: 0.15rem 0 0;
  color: #0f172a;
}

.claim-close {
  width: 2rem;
  height: 2rem;
  border: none;
  border-radius: 999px;
  background: #f1f5f9;
  color: #334155;
  cursor: pointer;
}

.claim-target {
  display: grid;
  gap: 0.2rem;
  margin-bottom: 1rem;
  padding: 0.9rem;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.11), rgba(59, 130, 246, 0.08));
}

.claim-target span,
.claim-target small,
.claim-field span {
  color: #64748b;
  font-size: 0.82rem;
}

.claim-target strong {
  color: #0f172a;
}

.claim-field {
  display: grid;
  gap: 0.45rem;
  margin-bottom: 0.85rem;
}

.claim-field textarea,
.claim-field input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #dbe4ef;
  border-radius: 12px;
  padding: 0.75rem 0.85rem;
  font: inherit;
  outline: none;
  background: #f8fafc;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.claim-field textarea:focus,
.claim-field input:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.12);
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

.claim-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
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
