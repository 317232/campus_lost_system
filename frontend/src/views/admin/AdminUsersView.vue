<script setup>
import { ref, reactive } from 'vue'
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

// ─── 列表 ─────────────────────────────────────────────
const fallbackUsers = [
  { id: 1, studentNo: '2023123456', name: '示例用户', email: '', phone: '', major: '', status: 'ACTIVE' },
]

const usersState = useRemoteCollection(() => adminApi.getUsers(), fallbackUsers)

// ─── 提示 ─────────────────────────────────────────────
const actionMsg = ref('')
const actionError = ref(false)
function notify(msg, isError = false) {
  actionMsg.value = msg
  actionError.value = isError
  setTimeout(() => { actionMsg.value = '' }, 3500)
}

// ─── 加载态 ────────────────────────────────────────────
const loadingId = ref(null)
const submitting = ref(false)

// ─── 弹窗状态 ──────────────────────────────────────────
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const emptyForm = () => ({ studentNo: '', name: '', password: '', phone: '', email: '', major: '' })
const form = reactive(emptyForm())
const formError = ref('')

function openCreate() {
  isEdit.value = false
  editingId.value = null
  Object.assign(form, emptyForm())
  formError.value = ''
  showModal.value = true
}

function openEdit(user) {
  isEdit.value = true
  editingId.value = user.id
  Object.assign(form, {
    studentNo: user.studentNo || '',
    name:      user.name      || '',
    password:  '',
    phone:     user.phone     || '',
    email:     user.email     || '',
    major:     user.major     || '',
  })
  formError.value = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

// ─── 提交（新增 / 编辑）────────────────────────────────
async function submitForm() {
  formError.value = ''
  submitting.value = true
  try {
    if (isEdit.value) {
      // 编辑：只传可改字段
      await adminApi.updateUser(editingId.value, {
        name:  form.name,
        phone: form.phone,
        email: form.email,
        major: form.major,
      })
      notify(`用户「${form.name}」信息已更新`)
    } else {
      // 新增
      await adminApi.createUser({
        studentNo: form.studentNo,
        name:      form.name,
        password:  form.password,
        phone:     form.phone,
        email:     form.email,
        major:     form.major,
      })
      notify(`用户「${form.name}」创建成功`)
    }
    showModal.value = false
    await usersState.reload()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '操作失败'
    formError.value = msg
  } finally {
    submitting.value = false
  }
}

// ─── 删除 ──────────────────────────────────────────────
async function deleteUser(user) {
  if (!confirm(`确定删除用户「${user.name || user.studentNo}」？此操作不可恢复。`)) return
  loadingId.value = user.id
  try {
    await adminApi.deleteUser(user.id)
    notify(`已删除用户：${user.name || user.studentNo}`)
    await usersState.reload()
  } catch (e) {
    notify(e?.response?.data?.message || e?.message || '删除失败', true)
  } finally {
    loadingId.value = null
  }
}

// ─── 禁用 / 启用 ───────────────────────────────────────
async function toggleStatus(user) {
  const newStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  loadingId.value = user.id
  try {
    await adminApi.updateUserStatus(user.id, newStatus)
    notify(`已${newStatus === 'DISABLED' ? '禁用' : '启用'}用户：${user.name || user.studentNo}`)
    await usersState.reload()
  } catch (e) {
    notify(e?.response?.data?.message || e?.message || '操作失败', true)
  } finally {
    loadingId.value = null
  }
}
</script>

<template>
  <section class="page-section">
    <!-- 标题栏 -->
    <div class="panel-header">
      <div>
        <p class="eyebrow">Role Control</p>
        <h2>用户管理</h2>
      </div>
      <button type="button" id="btn-create-user" @click="openCreate">+ 新增用户</button>
    </div>

    <!-- 提示 -->
    <p
      v-if="actionMsg"
      class="feedback"
      :class="actionError ? 'feedback-error' : 'feedback-success'"
    >{{ actionMsg }}</p>
    <p v-if="usersState.error" class="feedback feedback-error">用户数据加载失败，显示缓存数据。</p>

    <!-- 表格 -->
    <div class="table-shell">
      <table>
        <thead>
          <tr>
            <th>学号</th>
            <th>姓名</th>
            <th>手机</th>
            <th>邮箱</th>
            <th>专业</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in usersState.items" :key="user.id">
            <td>{{ user.studentNo || '—' }}</td>
            <td>{{ user.name     || '—' }}</td>
            <td>{{ user.phone    || '—' }}</td>
            <td>{{ user.email    || '—' }}</td>
            <td>{{ user.major    || '—' }}</td>
            <td>
              <span :class="user.status === 'ACTIVE' ? 'status-badge' : 'status-badge status-badge--off'">
                {{ user.status === 'ACTIVE' ? '正常' : '已禁用' }}
              </span>
            </td>
            <td class="inline-actions">
              <!-- 编辑 -->
              <button
                type="button"
                class="secondary"
                :disabled="loadingId === user.id"
                @click="openEdit(user)"
              >编辑</button>

              <!-- 禁用/启用 -->
              <button
                type="button"
                :class="user.status === 'ACTIVE' ? 'secondary' : ''"
                :disabled="loadingId === user.id"
                @click="toggleStatus(user)"
              >
                {{ loadingId === user.id ? '处理中…' : user.status === 'ACTIVE' ? '禁用' : '启用' }}
              </button>

              <!-- 删除 -->
              <button
                type="button"
                class="danger"
                :disabled="loadingId === user.id"
                @click="deleteUser(user)"
              >删除</button>
            </td>
          </tr>
          <tr v-if="usersState.items.length === 0">
            <td colspan="7" style="text-align:center;color:var(--color-muted,#888);">暂无用户数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增 / 编辑 弹窗 -->
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal-card" role="dialog" :aria-label="isEdit ? '编辑用户' : '新增用户'">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑用户' : '新增用户' }}</h3>
          <button type="button" class="modal-close" @click="closeModal">✕</button>
        </div>

        <form class="stack-form" @submit.prevent="submitForm">
          <!-- 学号（仅新增可填） -->
          <label>
            学号 <span v-if="!isEdit" style="color:#e55">*</span>
            <input
              v-model="form.studentNo"
              :disabled="isEdit"
              placeholder="请输入学号"
              required
            />
          </label>

          <!-- 姓名 -->
          <label>
            姓名 <span style="color:#e55">*</span>
            <input v-model="form.name" placeholder="请输入姓名" required />
          </label>

          <!-- 密码（仅新增必填） -->
          <label v-if="!isEdit">
            密码 <span style="color:#e55">*</span>
            <input v-model="form.password" type="password" placeholder="6-32位密码" required minlength="6" />
          </label>

          <!-- 手机 -->
          <label>
            手机号
            <input v-model="form.phone" placeholder="选填" />
          </label>

          <!-- 邮箱 -->
          <label>
            邮箱
            <input v-model="form.email" type="email" placeholder="选填" />
          </label>

          <!-- 专业 -->
          <label>
            专业
            <input v-model="form.major" placeholder="选填" />
          </label>

          <p v-if="formError" class="feedback feedback-error">{{ formError }}</p>

          <div class="modal-footer">
            <button type="button" class="secondary" @click="closeModal">取消</button>
            <button type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : isEdit ? '保存修改' : '创建用户' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* ── 弹窗蒙层 ────────────────────────────────── */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  backdrop-filter: blur(3px);
}

.modal-card {
  background: var(--color-surface, #1e1e2e);
  border: 1px solid var(--color-border, rgba(255,255,255,0.1));
  border-radius: 12px;
  padding: 2rem;
  width: min(480px, 90vw);
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: slideUp 0.2s ease;
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to   { transform: translateY(0);    opacity: 1; }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  color: var(--color-muted, #888);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: color 0.15s;
}
.modal-close:hover { color: var(--color-text, #fff); }

.modal-footer {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

/* ── 危险按钮 ─────────────────────────────────── */
button.danger {
  background: rgba(220, 38, 38, 0.15);
  color: #f87171;
  border: 1px solid rgba(220, 38, 38, 0.3);
}
button.danger:hover:not(:disabled) {
  background: rgba(220, 38, 38, 0.3);
}

/* ── 操作列间距 ───────────────────────────────── */
.inline-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
</style>
