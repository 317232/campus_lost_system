<script setup>
import { ref, reactive } from 'vue'
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

// ─── 列表 ─────────────────────────────────────────────────────
const noticesState = useRemoteCollection(() => adminApi.getNotices(), [])

// ─── 提示 ─────────────────────────────────────────────────────
const msg     = ref('')
const isError = ref(false)
function notify(text, err = false) {
  msg.value = text; isError.value = err
  setTimeout(() => { msg.value = '' }, 3500)
}

// ─── 加载 ─────────────────────────────────────────────────────
const loadingId  = ref(null)
const submitting = ref(false)

// ─── 弹窗 ─────────────────────────────────────────────────────
const showModal = ref(false)
const isEdit    = ref(false)
const editId    = ref(null)
const formErr   = ref('')

const emptyForm = () => ({ title: '', content: '', published: false })
const form = reactive(emptyForm())

function openCreate() {
  isEdit.value = false; editId.value = null
  Object.assign(form, emptyForm()); formErr.value = ''
  showModal.value = true
}
function openEdit(n) {
  isEdit.value = true; editId.value = n.id
  Object.assign(form, { title: n.title || '', content: n.content || '', published: !!n.published })
  formErr.value = ''; showModal.value = true
}
function closeModal() { showModal.value = false }

async function submit() {
  formErr.value = ''; submitting.value = true
  try {
    if (isEdit.value) {
      await adminApi.updateNotice(editId.value, { title: form.title, content: form.content, published: form.published })
      notify(`公告「${form.title}」已更新`)
    } else {
      await adminApi.createNotice({ title: form.title, content: form.content, published: form.published })
      notify(`公告「${form.title}」发布成功`)
    }
    showModal.value = false
    await noticesState.reload()
  } catch (e) {
    formErr.value = e?.response?.data?.message || e?.message || '操作失败'
  } finally { submitting.value = false }
}

async function deleteNotice(n) {
  if (!confirm(`确定删除公告「${n.title}」？`)) return
  loadingId.value = n.id
  try {
    await adminApi.deleteNotice(n.id)
    notify(`已删除公告：${n.title}`)
    await noticesState.reload()
  } catch (e) { notify(e?.response?.data?.message || '删除失败', true)
  } finally { loadingId.value = null }
}
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Notice CRUD</p>
        <h2>公告管理</h2>
      </div>
      <button type="button" id="btn-create-notice" @click="openCreate">+ 新增公告</button>
    </div>

    <p v-if="msg" class="feedback" :class="isError ? 'feedback-error' : 'feedback-success'">{{ msg }}</p>
    <p v-if="noticesState.error" class="feedback feedback-error">公告接口不可用，显示缓存数据。</p>

    <div class="table-shell">
      <table>
        <thead>
          <tr>
            <th>标题</th>
            <th>状态</th>
            <th>发布时间</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="n in noticesState.items" :key="n.id">
            <td>{{ n.title }}</td>
            <td>
              <span :class="n.published ? 'status-badge' : 'status-badge status-badge--off'">
                {{ n.published ? '已发布' : '草稿' }}
              </span>
            </td>
            <td>{{ n.publishedAt ? n.publishedAt.slice(0,16).replace('T',' ') : '—' }}</td>
            <td>{{ n.createTime ? n.createTime.slice(0,16).replace('T',' ') : '—' }}</td>
            <td class="inline-actions">
              <button type="button" class="secondary" :disabled="loadingId === n.id" @click="openEdit(n)">编辑</button>
              <button type="button" class="danger" :disabled="loadingId === n.id" @click="deleteNotice(n)">
                {{ loadingId === n.id ? '处理中…' : '删除' }}
              </button>
            </td>
          </tr>
          <tr v-if="noticesState.items.length === 0">
            <td colspan="5" class="empty-td">暂无公告</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗 -->
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal-card" role="dialog">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑公告' : '新增公告' }}</h3>
          <button type="button" class="modal-close" @click="closeModal">✕</button>
        </div>
        <form class="stack-form" @submit.prevent="submit">
          <label>标题 <span class="req">*</span><input v-model="form.title" required placeholder="请输入公告标题" /></label>
          <label>内容 <span class="req">*</span><textarea v-model="form.content" required rows="6" placeholder="请输入公告内容" /></label>
          <label class="checkbox-row">
            <input type="checkbox" v-model="form.published" />
            立即发布
          </label>
          <p v-if="formErr" class="feedback feedback-error">{{ formErr }}</p>
          <div class="modal-footer">
            <button type="button" class="secondary" @click="closeModal">取消</button>
            <button type="submit" :disabled="submitting">{{ submitting ? '提交中…' : isEdit ? '保存修改' : '发布公告' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.inline-actions { display: flex; gap: .5rem; flex-wrap: wrap; }
.empty-td { text-align: center; color: var(--color-muted, #888); padding: 1.5rem 0; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 200; backdrop-filter: blur(3px); }
.modal-card { background: var(--color-surface, #1e1e2e); border: 1px solid var(--color-border, rgba(255,255,255,.1)); border-radius: 12px; padding: 2rem; width: min(520px, 90vw); box-shadow: 0 20px 60px rgba(0,0,0,.5); animation: slideUp .2s ease; }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; }
.modal-header h3 { margin: 0; font-size: 1.1rem; font-weight: 600; }
.modal-close { background: none; border: none; cursor: pointer; font-size: 1rem; color: var(--color-muted, #888); padding: .25rem .5rem; border-radius: 4px; }
.modal-footer { display: flex; gap: .75rem; justify-content: flex-end; margin-top: 1.5rem; }
.req { color: #e55; }
.checkbox-row { display: flex; align-items: center; gap: .5rem; cursor: pointer; }
textarea { width: 100%; resize: vertical; font-family: inherit; font-size: .9rem; }
button.danger { background: rgba(220,38,38,.15); color: #f87171; border: 1px solid rgba(220,38,38,.3); }
button.danger:hover:not(:disabled) { background: rgba(220,38,38,.3); }
</style>
