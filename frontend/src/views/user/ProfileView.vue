<script setup>
import { ref, computed, watch } from 'vue'
import { claimApi, userApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { useRequest } from '@/composables/useRequest'

// ─── 数据加载 ─────────────────────────────────────────────────
const profileFallback = [
  {
    id: 1,
    name: '加载中…',
    studentNo: '—',
    phone: '',
    email: '',
    major: '',
    avatarUrl: '',
    status: 'ACTIVE',
    createTime: null,
  },
]

const profileState = useRemoteCollection(() => userApi.getProfile(), profileFallback, {
  select: (p) => (p ? [p] : profileFallback),
})
const lostState  = useRemoteCollection(() => userApi.getMyLostItems(), [])
const foundState = useRemoteCollection(() => userApi.getMyFoundItems(), [])
const claimState = useRemoteCollection(() => claimApi.listMine(), [])

const profile = computed(() => profileState.items[0] || profileFallback[0])

// ─── 编辑表单 ─────────────────────────────────────────────────
const form = ref({ name: '', phone: '', email: '', major: '', avatarUrl: '' })
const saveMsg   = ref('')
const saveFail  = ref(false)

// 当 profile 数据就绪时同步表单
watch(profile, (p) => {
  form.value = {
    name:      p.name      || '',
    phone:     p.phone     || '',
    email:     p.email     || '',
    major:     p.major     || p.bio || '',   // bio 字段后端映射 major
    avatarUrl: p.avatarUrl || '',
  }
}, { immediate: true })

const saveReq = useRequest(() =>
  userApi.updateProfile({
    displayName: form.value.name,
    phone:       form.value.phone,
    email:       form.value.email,
    avatarUrl:   form.value.avatarUrl,
    bio:         form.value.major,   // bio → User.major（后端映射）
  })
)

async function save() {
  saveMsg.value = ''
  saveFail.value = false
  await saveReq.run()
  if (saveReq.error.value) {
    saveMsg.value = `保存失败：${saveReq.error.value}`
    saveFail.value = true
  } else {
    saveMsg.value = '保存成功！'
    await profileState.reload()
  }
  setTimeout(() => { saveMsg.value = '' }, 3500)
}

// ─── 物品状态辅助 ─────────────────────────────────────────────
const auditLabel = {
  PUBLISHED:      { text: '已发布', cls: 'badge-ok' },
  PENDING_REVIEW: { text: '审核中', cls: 'badge-pending' },
  REJECTED:       { text: '已驳回', cls: 'badge-err' },
  CLAIMED:        { text: '已认领', cls: 'badge-ok' },
}
function auditInfo(status) {
  return auditLabel[status] || { text: status || '—', cls: '' }
}

// ─── 认领统计 ─────────────────────────────────────────────────
const pendingClaims = computed(() =>
  claimState.items.filter((c) => c.status === 'PENDING').length
)
</script>

<template>
  <div class="profile-root">

    <!-- ── 顶部用户卡片 ────────────────────────────────────── -->
    <section class="user-hero panel">
      <div class="avatar-wrap">
        <img
          v-if="profile.avatarUrl"
          :src="profile.avatarUrl"
          :alt="profile.name"
          class="avatar-img"
        />
        <div v-else class="avatar-placeholder">
          {{ (profile.name || '?')[0].toUpperCase() }}
        </div>
      </div>

      <div class="hero-info">
        <h2 class="hero-name">{{ profile.name || '—' }}</h2>
        <p class="hero-meta">
          <span>{{ profile.studentNo || '—' }}</span>
          <span v-if="profile.major" class="divider">·</span>
          <span v-if="profile.major">{{ profile.major }}</span>
        </p>
        <span
          class="status-badge"
          :class="profile.status !== 'ACTIVE' ? 'status-badge--off' : ''"
        >{{ profile.status === 'ACTIVE' ? '正常' : '已禁用' }}</span>
      </div>

      <!-- 快速统计 -->
      <div class="hero-stats">
        <div class="stat-item">
          <span class="stat-num">{{ lostState.items.length }}</span>
          <span class="stat-lbl">发布失物</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ foundState.items.length }}</span>
          <span class="stat-lbl">发布招领</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ claimState.items.length }}</span>
          <span class="stat-lbl">认领申请</span>
        </div>
        <div class="stat-item" v-if="pendingClaims > 0">
          <span class="stat-num pending">{{ pendingClaims }}</span>
          <span class="stat-lbl">待审核</span>
        </div>
      </div>
    </section>

    <div class="profile-grid">

      <!-- ── 编辑资料面板 ──────────────────────────────────── -->
      <article class="panel">
        <div class="panel-header">
          <div>
            <p class="eyebrow">Profile</p>
            <h2>编辑个人资料</h2>
          </div>
        </div>

        <form class="stack-form" @submit.prevent="save">
          <label>
            姓名
            <input v-model="form.name" placeholder="请输入姓名" />
          </label>
          <label>
            学号（不可修改）
            <input :value="profile.studentNo" disabled />
          </label>
          <label>
            专业
            <input v-model="form.major" placeholder="请输入专业" />
          </label>
          <label>
            手机号
            <input v-model="form.phone" placeholder="请输入手机号" />
          </label>
          <label>
            邮箱
            <input v-model="form.email" type="email" placeholder="请输入邮箱" />
          </label>
          <label>
            头像 URL
            <input v-model="form.avatarUrl" placeholder="图片链接（选填）" />
          </label>

          <p
            v-if="saveMsg"
            class="feedback"
            :class="saveFail ? 'feedback-error' : 'feedback-success'"
          >{{ saveMsg }}</p>

          <button type="submit" :disabled="saveReq.loading.value">
            {{ saveReq.loading.value ? '保存中…' : '保存修改' }}
          </button>
        </form>
      </article>

      <!-- ── 发布的失物列表 ────────────────────────────────── -->
      <article class="panel">
        <div class="panel-header">
          <div>
            <p class="eyebrow">Lost Items</p>
            <h2>我发布的失物</h2>
          </div>
          <span class="count-badge">{{ lostState.items.length }}</span>
        </div>

        <div v-if="lostState.loading" class="loading-hint">加载中…</div>
        <div v-else-if="lostState.items.length === 0" class="empty-hint">暂无记录</div>
        <ul v-else class="item-list">
          <li v-for="item in lostState.items" :key="item.id" class="item-row">
            <div class="item-main">
              <span class="item-title">{{ item.title || item.itemName || '—' }}</span>
              <span class="item-meta">{{ item.category }} · {{ item.location }}</span>
            </div>
            <span :class="['audit-badge', auditInfo(item.status).cls]">
              {{ auditInfo(item.status).text }}
            </span>
          </li>
        </ul>
      </article>

      <!-- ── 发布的招领列表 ────────────────────────────────── -->
      <article class="panel">
        <div class="panel-header">
          <div>
            <p class="eyebrow">Found Items</p>
            <h2>我发布的招领</h2>
          </div>
          <span class="count-badge">{{ foundState.items.length }}</span>
        </div>

        <div v-if="foundState.loading" class="loading-hint">加载中…</div>
        <div v-else-if="foundState.items.length === 0" class="empty-hint">暂无记录</div>
        <ul v-else class="item-list">
          <li v-for="item in foundState.items" :key="item.id" class="item-row">
            <div class="item-main">
              <span class="item-title">{{ item.title || item.itemName || '—' }}</span>
              <span class="item-meta">{{ item.category }} · {{ item.location }}</span>
            </div>
            <span :class="['audit-badge', auditInfo(item.status).cls]">
              {{ auditInfo(item.status).text }}
            </span>
          </li>
        </ul>
      </article>

      <!-- ── 认领申请列表 ────────────────────────────────────── -->
      <article class="panel">
        <div class="panel-header">
          <div>
            <p class="eyebrow">Claims</p>
            <h2>我的认领申请</h2>
          </div>
          <span class="count-badge" :class="pendingClaims > 0 ? 'count-badge--warn' : ''">
            {{ claimState.items.length }}
          </span>
        </div>

        <div v-if="claimState.loading" class="loading-hint">加载中…</div>
        <div v-else-if="claimState.items.length === 0" class="empty-hint">暂无认领记录</div>
        <ul v-else class="item-list">
          <li v-for="c in claimState.items" :key="c.id" class="item-row">
            <div class="item-main">
              <span class="item-title">{{ c.itemName || '认领 #' + c.id }}</span>
              <span class="item-meta">{{ c.createTime || '' }}</span>
            </div>
            <span :class="['audit-badge',
              c.status === 'APPROVED'      ? 'badge-ok'      :
              c.status === 'REJECTED'      ? 'badge-err'     :
              (c.status === 'APPLIED' || c.status === 'CHECKING' || c.status === 'REVIEW_PENDING') ? 'badge-pending'  : '']">
              {{
                c.status === 'APPROVED'       ? '已通过'   :
                c.status === 'REJECTED'       ? '已拒绝'   :
                c.status === 'APPLIED'        ? '待审核'   :
                c.status === 'CHECKING'       ? '核对中'   :
                c.status === 'REVIEW_PENDING' ? '待审核'   :
                c.status === 'CANCELLED'      ? '已取消'   : c.status || '—'
              }}
            </span>
          </li>
        </ul>
      </article>
    </div><!-- /profile-grid -->
  </div><!-- /profile-root -->
</template>

<style scoped>
/* ── 整体布局 ──────────────────────────────────────────────── */
.profile-root {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.5rem;
}

/* ── 顶部英雄卡片 ───────────────────────────────────────────── */
.user-hero {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar-img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border, rgba(255,255,255,.15));
}

.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.hero-name {
  margin: 0 0 .25rem;
  font-size: 1.3rem;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hero-meta {
  margin: 0 0 .5rem;
  font-size: .875rem;
  color: var(--color-muted, #888);
  display: flex;
  gap: .4rem;
  flex-wrap: wrap;
}

.divider { opacity: .5; }

/* ── 统计数字栏 ─────────────────────────────────────────────── */
.hero-stats {
  display: flex;
  gap: 1.5rem;
  margin-left: auto;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: .15rem;
}

.stat-num {
  font-size: 1.4rem;
  font-weight: 700;
  line-height: 1;
}

.stat-num.pending {
  color: #f59e0b;
}

.stat-lbl {
  font-size: .75rem;
  color: var(--color-muted, #888);
}

/* ── 数量角标 ─────────────────────────────────────────────── */
.count-badge {
  font-size: .8rem;
  font-weight: 600;
  background: var(--color-surface-2, rgba(255,255,255,.06));
  border: 1px solid var(--color-border, rgba(255,255,255,.1));
  border-radius: 999px;
  padding: .2rem .65rem;
  color: var(--color-muted, #888);
  white-space: nowrap;
}

.count-badge--warn {
  background: rgba(245,158,11,.15);
  border-color: rgba(245,158,11,.35);
  color: #f59e0b;
}

/* ── 物品列表 ─────────────────────────────────────────────── */
.item-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: .75rem;
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: .75rem;
  padding: .65rem .75rem;
  border-radius: 8px;
  background: var(--color-surface-2, rgba(255,255,255,.04));
  transition: background .15s;
}

.item-row:hover {
  background: var(--color-surface-3, rgba(255,255,255,.08));
}

.item-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.item-title {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: .9rem;
}

.item-meta {
  font-size: .775rem;
  color: var(--color-muted, #888);
  margin-top: .1rem;
}

/* ── 审核状态角标 ─────────────────────────────────────────── */
.audit-badge {
  font-size: .75rem;
  font-weight: 600;
  border-radius: 999px;
  padding: .2rem .65rem;
  white-space: nowrap;
  flex-shrink: 0;
}

.badge-ok {
  background: rgba(34,197,94,.15);
  border: 1px solid rgba(34,197,94,.3);
  color: #4ade80;
}

.badge-pending {
  background: rgba(245,158,11,.15);
  border: 1px solid rgba(245,158,11,.3);
  color: #fbbf24;
}

.badge-err {
  background: rgba(239,68,68,.15);
  border: 1px solid rgba(239,68,68,.3);
  color: #f87171;
}

/* ── 空态/加载 ────────────────────────────────────────────── */
.empty-hint,
.loading-hint {
  text-align: center;
  padding: 1.5rem 0;
  color: var(--color-muted, #888);
  font-size: .875rem;
}
</style>
