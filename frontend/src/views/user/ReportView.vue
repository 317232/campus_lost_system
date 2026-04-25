<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { reportApi } from '@/api/modules'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const form = reactive({
  targetType: 'ITEM',
  targetId: null,
  reason: '',
  detail: ''
})

const targetTypeLabel = computed(() => {
  const opt = targetTypeOptions.find(o => o.value === form.targetType)
  return opt ? opt.label : form.targetType
})

onMounted(() => {
  if (route.query.targetType) {
    form.targetType = route.query.targetType
  }
  if (route.query.targetId) {
    form.targetId = route.query.targetId
  }
})

const targetTypeOptions = [
  { value: 'ITEM', label: '物品信息' },
  { value: 'CLAIM', label: '认领申请' },
  { value: 'ANNOUNCEMENT', label: '公告' },
  { value: 'USER', label: '用户' }
];

const reasonOptions = [
  '虚假信息',
  '联系方式不可用',
  '涉及隐私',
  '广告/垃圾信息',
  '其他'
]

async function submitReport() {
  if (!form.reason) {
    errorMsg.value = '请选择举报原因'
    return
  }
  if (!form.targetId) {
    errorMsg.value = '请输入被举报对象的ID'
    return
  }

  errorMsg.value = ''
  successMsg.value = ''
  submitting.value = true

  try {
    await reportApi.createReport({
      targetType: form.targetType,
      targetId: parseInt(form.targetId),
      reason: form.reason,
      detail: form.detail
    })
    successMsg.value = '举报提交成功，我们会尽快处理'
    form.targetId = ''
    form.reason = ''
    form.detail = ''
    setTimeout(() => {
      router.push('/profile')
    }, 2000)
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="report-view">
    <h1>举报反馈</h1>
    <p class="subtitle">请提供举报详情，我们会尽快处理</p>

    <div v-if="successMsg" class="success-message">{{ successMsg }}</div>
    <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>

    <form @submit.prevent="submitReport" class="report-form">
      <div class="form-group">
        <label>举报类型</label>
        <select v-model="form.targetType">
          <option v-for="opt in targetTypeOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <div class="form-group" v-if="route.query.targetId">
        <label>被举报对象</label>
        <div class="target-info">
          <span class="target-badge">{{ targetTypeLabel }}</span>
          <span> #{{ form.targetId }}</span>
        </div>
      </div>
      <div class="form-group" v-else>
        <label>被举报对象ID</label>
        <input v-model="form.targetId" type="number" placeholder="请输入被举报内容的ID" required />
      </div>

      <div class="form-group">
        <label>举报原因</label>
        <select v-model="form.reason" required>
          <option value="">请选择举报原因</option>
          <option v-for="r in reasonOptions" :key="r" :value="r">{{ r }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>补充说明（可选）</label>
        <textarea v-model="form.detail" placeholder="请详细描述问题..." rows="4"></textarea>
      </div>

      <button type="submit" :disabled="submitting" class="submit-btn">
        {{ submitting ? '提交中...' : '提交举报' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.report-view {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}
h1 {
  font-size: 24px;
  margin-bottom: 8px;
}
.subtitle {
  color: #666;
  margin-bottom: 24px;
}
.error-message {
  background: #fee;
  color: #c00;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}
.success-message {
  background: #efe;
  color: #060;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}
.report-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-group label {
  font-weight: 500;
  font-size: 14px;
}
.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
.form-group textarea {
  resize: vertical;
}
.target-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 14px;
}
.target-badge {
  background: #e3f2fd;
  color: #1976d2;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.submit-btn {
  background: #007bff;
  color: white;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}
.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>