<script setup>
import { ref, reactive } from 'vue'
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

// ─── 列表 ─────────────────────────────────────────────────────
const reportsState = useRemoteCollection(() => adminApi.getReports(), [])

// ─── 提示 ─────────────────────────────────────────────────────
const msg = ref(''); const isError = ref(false)
function notify(text, err = false) {
  msg.value = text; isError.value = err
  setTimeout(() => { msg.value = '' }, 3500)
}

// ─── 加载态 ────────────────────────────────────────────────────
const loadingId  = ref(null)
const submitting = ref(false)

// ─── 处理弹窗 ──────────────────────────────────────────────────
const showModal = ref(false)
const editId    = ref(null)
const formErr   = ref('')
const form = reactive({ status: 'RESOLVED', handleRemark: '' })

const statusOptions = [
  { value: 'PROCESSING', label: '处理中' },
  { value: 'RESOLVED',   label: '已解决' },
  { value: 'REJECTED',   label: '已驳回' },
]

function openHandle(r) {
  editId.value = r.id
  form.status = r.status === 'PENDING' ? 'PROCESSING' : (r.status || 'RESOLVED')
  form.handleRemark = r.handleRemark || ''
  formErr.value = ''
  showModal.value = true
}
function closeModal() { showModal.value = false }

async function submitHandle() {
  formErr.value = ''; submitting.value = true
  try {
    await adminApi.handleReport(editId.value, form.status, form.handleRemark)
    notify(`举报 #${editId.value} 已处理`)
    showModal.value = false
    await reportsState.reload()
  } catch (e) {
    formErr.value = e?.response?.data?.message || e?.message || '操作失败'
  } finally { submitting.value = false }
}

async function deleteReport(r) {
  if (!confirm(`确定删除举报记录 #${r.id}？此操作不可恢复。`)) return
  loadingId.value = r.id
  try {
    await adminApi.deleteReport(r.id)
    notify(`已删除举报记录 #${r.id}`)
    await reportsState.reload()
  } catch (e) { notify(e?.response?.data?.message || '删除失败', true)
  } finally { loadingId.value = null }
}

// ─── 辅助 ─────────────────────────────────────────────────────
function statusLabel(s) {
  return { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', REJECTED: '已驳回' }[s] || s || '—'
}
function statusCls(s) {
  if (s === 'RESOLVED') return 'badge-ok'
  if (s === 'REJECTED') return 'badge-err'
  if (s === 'PROCESSING') return 'badge-pending'
  return 'badge-pending'
}
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Report Queue</p>
        <h2>举报处理</h2>
      </div>
      <button type="button" class="secondary" @click="reportsState.reload()">刷新</button>
    </div>

    <p v-if="msg" class="feedback" :class="isError ? 'feedback-error' : 'feedback-success'">{{ msg }}</p>
    <p v-if="reportsState.error" class="feedback feedback-error">举报接口不可用，请联系管理员。</p>

    <div class="table-shell">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>举报对象</th>
            <th>目标 ID</th>
            <th>举报原因</th>
            <th>状态</th>
            <th>处理备注</th>
            <th>举报时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in reportsState.items" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.targetType || '—' }}</td>
            <td>{{ r.targetId || '—' }}</td>
            <td class="reason-cell">{{ r.reason || '—' }}</td>
            <td>
              <span :class="['audit-badge', statusCls(r.status)]">{{ statusLabel(r.status) }}</span>
            </td>
            <td class="remark-cell">{{ r.handleRemark || '—' }}</td>
            <td>{{ r.createTime ? r.createTime.slice(0,16).replace('T',' ') : '—' }}</td>
            <td class="inline-actions">
              <button
                type="button"
                class="secondary"
                :disabled="loadingId === r.id"
                @click="openHandle(r)"
              >处理</button>
              <button
                type="button"
                class="danger"
                :disabled="loadingId === r.id"
                @click="deleteReport(r)"
              >{{ loadingId === r.id ? '…' : '删除' }}</button>
            </td>
          </tr>
          <tr v-if="reportsState.items.length === 0">
            <td colspan="8" class="empty-td">暂无举报记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 处理弹窗 -->
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal-card" role="dialog">
        <div class="modal-header">
          <h3>处理举报 #{{ editId }}</h3>
          <button type="button" class="modal-close" @click="closeModal">✕</button>
        </div>
        <form class="stack-form" @submit.prevent="submitHandle">
          <label>处理结果
            <select v-model="form.status">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </label>
          <label>处理备注
            <textarea v-model="form.handleRemark" rows="4" placeholder="请填写处理说明（选填）" />
          </label>
          <p v-if="formErr" class="feedback feedback-error">{{ formErr }}</p>
          <div class="modal-footer">
            <button type="button" class="secondary" @click="closeModal">取消</button>
            <button type="submit" :disabled="submitting">{{ submitting ? '提交中…' : '确认处理' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.inline-actions { display: flex; gap: .5rem; flex-wrap: wrap; }
.empty-td { text-align: center; color: var(--color-muted, #888); padding: 1.5rem 0; }
.reason-cell, .remark-cell { max-width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 弹窗 */
.modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 200; backdrop-filter: blur(3px); }
.modal-card { background: var(--color-surface, #1e1e2e); border: 1px solid var(--color-border, rgba(255,255,255,.1)); border-radius: 12px; padding: 2rem; width: min(480px, 90vw); box-shadow: 0 20px 60px rgba(0,0,0,.5); animation: slideUp .2s ease; }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; }
.modal-header h3 { margin: 0; font-size: 1.1rem; font-weight: 600; }
.modal-close { background: none; border: none; cursor: pointer; font-size: 1rem; color: var(--color-muted, #888); padding: .25rem .5rem; border-radius: 4px; }
.modal-footer { display: flex; gap: .75rem; justify-content: flex-end; margin-top: 1.5rem; }

/* 状态角标 */
.audit-badge { font-size: .75rem; font-weight: 600; border-radius: 999px; padding: .2rem .65rem; white-space: nowrap; }
.badge-ok      { background: rgba(34,197,94,.15); border: 1px solid rgba(34,197,94,.3); color: #4ade80; }
.badge-pending { background: rgba(245,158,11,.15); border: 1px solid rgba(245,158,11,.3); color: #fbbf24; }
.badge-err     { background: rgba(239,68,68,.15);  border: 1px solid rgba(239,68,68,.3);  color: #f87171; }

button.danger { background: rgba(220,38,38,.15); color: #f87171; border: 1px solid rgba(220,38,38,.3); }
button.danger:hover:not(:disabled) { background: rgba(220,38,38,.3); }

select, textarea { width: 100%; padding: .5rem .75rem; background: var(--color-surface-2, #2a2a3e); border: 1px solid var(--color-border, rgba(255,255,255,.12)); border-radius: 6px; color: inherit; font-size: .9rem; font-family: inherit; resize: vertical; }
</style>
