<script setup>
import { ref, reactive } from 'vue'
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const catsState = useRemoteCollection(() => adminApi.getCategories(), [])
const msg = ref(''); const isError = ref(false)
function notify(text, err = false) { msg.value = text; isError.value = err; setTimeout(() => { msg.value = '' }, 3500) }
const loadingId = ref(null); const submitting = ref(false)
const showModal = ref(false); const isEdit = ref(false); const editId = ref(null); const formErr = ref('')
const emptyForm = () => ({ name: '', sortOrder: 0, status: 'ENABLED' })
const form = reactive(emptyForm())

function openCreate() { isEdit.value = false; editId.value = null; Object.assign(form, emptyForm()); formErr.value = ''; showModal.value = true }
function openEdit(c) { isEdit.value = true; editId.value = c.id; Object.assign(form, { name: c.name || '', sortOrder: c.sortOrder ?? 0, status: c.status || 'ENABLED' }); formErr.value = ''; showModal.value = true }
function closeModal() { showModal.value = false }

async function submit() {
  formErr.value = ''; submitting.value = true
  try {
    const p = { name: form.name, sortOrder: Number(form.sortOrder), status: form.status }
    if (isEdit.value) { await adminApi.updateCategory(editId.value, p); notify(`分类「${form.name}」已更新`) }
    else { await adminApi.createCategory(p); notify(`分类「${form.name}」创建成功`) }
    showModal.value = false; await catsState.reload()
  } catch (e) { formErr.value = e?.response?.data?.message || e?.message || '操作失败'
  } finally { submitting.value = false }
}

async function toggleStatus(c) {
  const ns = c.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'; loadingId.value = c.id
  try { await adminApi.updateCategory(c.id, { name: c.name, sortOrder: c.sortOrder, status: ns }); notify(`已${ns === 'DISABLED' ? '禁用' : '启用'}：${c.name}`); await catsState.reload()
  } catch (e) { notify('操作失败', true) } finally { loadingId.value = null }
}

async function deleteCategory(c) {
  if (!confirm(`确定删除分类「${c.name}」？`)) return; loadingId.value = c.id
  try { await adminApi.deleteCategory(c.id); notify(`已删除：${c.name}`); await catsState.reload()
  } catch (e) { notify('删除失败', true) } finally { loadingId.value = null }
}
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div><p class="eyebrow">Category CRUD</p><h2>物品分类管理</h2></div>
      <button type="button" id="btn-create-category" @click="openCreate">+ 新增分类</button>
    </div>
    <p v-if="msg" class="feedback" :class="isError ? 'feedback-error' : 'feedback-success'">{{ msg }}</p>
    <div class="table-shell">
      <table>
        <thead><tr><th>名称</th><th>编号</th><th>排序</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="c in catsState.items" :key="c.id">
            <td>{{ c.name }}</td>
            <td><code>{{ c.bizId || '—' }}</code></td>
            <td>{{ c.sortOrder ?? 0 }}</td>
            <td><span :class="c.status === 'ENABLED' ? 'status-badge' : 'status-badge status-badge--off'">{{ c.status === 'ENABLED' ? '启用' : '禁用' }}</span></td>
            <td>{{ c.createTime ? c.createTime.slice(0,16).replace('T',' ') : '—' }}</td>
            <td class="inline-actions">
              <button class="secondary" :disabled="loadingId===c.id" @click="openEdit(c)">编辑</button>
              <button class="secondary" :disabled="loadingId===c.id" @click="toggleStatus(c)">{{ c.status==='ENABLED'?'禁用':'启用' }}</button>
              <button class="danger" :disabled="loadingId===c.id" @click="deleteCategory(c)">删除</button>
            </td>
          </tr>
          <tr v-if="!catsState.items.length"><td colspan="6" class="empty-td">暂无分类数据</td></tr>
        </tbody>
      </table>
    </div>
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header"><h3>{{ isEdit?'编辑分类':'新增分类' }}</h3><button class="modal-close" @click="closeModal">✕</button></div>
        <form class="stack-form" @submit.prevent="submit">
          <label>分类名称 <span class="req">*</span><input v-model="form.name" required placeholder="如：电子设备、证件文件" /></label>
          <label>排序值<input v-model.number="form.sortOrder" type="number" min="0" /></label>
          <label>状态<select v-model="form.status"><option value="ENABLED">启用</option><option value="DISABLED">禁用</option></select></label>
          <p v-if="formErr" class="feedback feedback-error">{{ formErr }}</p>
          <div class="modal-footer">
            <button type="button" class="secondary" @click="closeModal">取消</button>
            <button type="submit" :disabled="submitting">{{ submitting?'提交中…':isEdit?'保存':'创建' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.inline-actions{display:flex;gap:.5rem;flex-wrap:wrap}.empty-td{text-align:center;color:var(--color-muted,#888);padding:1.5rem 0}
.modal-backdrop{position:fixed;inset:0;background:rgba(0,0,0,.5);display:flex;align-items:center;justify-content:center;z-index:200;backdrop-filter:blur(3px)}
.modal-card{background:var(--color-surface,#1e1e2e);border:1px solid var(--color-border,rgba(255,255,255,.1));border-radius:12px;padding:2rem;width:min(460px,90vw);box-shadow:0 20px 60px rgba(0,0,0,.5);animation:slideUp .2s ease}
@keyframes slideUp{from{transform:translateY(20px);opacity:0}to{transform:translateY(0);opacity:1}}
.modal-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:1.5rem}.modal-header h3{margin:0;font-size:1.1rem;font-weight:600}
.modal-close{background:none;border:none;cursor:pointer;font-size:1rem;color:var(--color-muted,#888);padding:.25rem .5rem;border-radius:4px}
.modal-footer{display:flex;gap:.75rem;justify-content:flex-end;margin-top:1.5rem}.req{color:#e55}
button.danger{background:rgba(220,38,38,.15);color:#f87171;border:1px solid rgba(220,38,38,.3)}
button.danger:hover:not(:disabled){background:rgba(220,38,38,.3)}
code{font-size:.8rem;opacity:.7}
select{width:100%;padding:.5rem .75rem;background:var(--color-surface-2,#2a2a3e);border:1px solid var(--color-border,rgba(255,255,255,.12));border-radius:6px;color:inherit;font-size:.9rem}
</style>
