<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { itemApi, categoriesApi } from '@/api/modules'
import { useAuth } from '@/composables/useAuth'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const route = useRoute()
const router = useRouter()
const { isAuthenticated } = useAuth()

const isFoundMode = computed(() => route.params.mode === 'found')
const title = computed(() => (isFoundMode.value ? '发布招领信息' : '发布失物信息'))
const eyebrow = computed(() => (isFoundMode.value ? 'Found Item Form' : 'Lost Item Form'))
const guestPrompt = computed(() => (isFoundMode.value ? '登录后即可发布招领线索。' : '登录后即可发布失物信息。'))

const categoriesState = useRemoteCollection(
  () => (isAuthenticated.value ? categoriesApi.list() : Promise.resolve([])),
  [],
)

// 表单数据
const form = reactive({
  itemName: '',
  category: '',
  zone: '',
  location: '',
  timeLabel: '',
  description: '',
  contactType: '微信',
  contactValue: '',
  images: [],
})

const imageInput = ref('')
const fileInput = ref(null)
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const uploadingImages = ref(false)

function addImage() {
  const url = imageInput.value.trim()
  if (url && !form.images.includes(url)) {
    form.images.push(url)
  }
  imageInput.value = ''
}

function removeImage(idx) {
  form.images.splice(idx, 1)
}

async function handleFileSelect(event) {
  const files = event.target.files
  if (!files || files.length === 0) return

  uploadingImages.value = true
  try {
    for (const file of Array.from(files)) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        errorMsg.value = '只支持上传图片文件'
        return
      }
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        errorMsg.value = '图片大小不能超过5MB'
        return
      }

      const uploadedUrl = await itemApi.uploadFile(file)
      if (uploadedUrl && !form.images.includes(uploadedUrl)) {
        form.images.push(uploadedUrl)
      }
    }
  } catch (e) {
    errorMsg.value = e?.message || '图片上传失败，请重试'
  } finally {
    uploadingImages.value = false
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

function triggerFileInput() {
  fileInput.value?.click()
}

async function submit() {
  errorMsg.value = ''

  if (!form.itemName.trim()) {
    errorMsg.value = '请输入物品名称'
    return
  }
  if (!form.location.trim()) {
    errorMsg.value = '请输入地点'
    return
  }

  submitting.value = true
  try {
    await itemApi.createItem({
      scene: isFoundMode.value ? 'found' : 'lost',
      title: form.itemName,
      itemName: form.itemName,
      category: form.category,
      zone: form.zone,
      location: form.location,
      timeLabel: form.timeLabel,
      description: form.description,
      contactType: form.contactType,
      contactValue: form.contactValue,
      images: form.images,
    })
    successMsg.value = '发布成功！管理员将在24小时内审核，审核结果可在个人中心查看。'
    setTimeout(() => router.push('/profile'), 2000)
  } catch (e) {
    errorMsg.value = e?.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
        <p class="section-copy">
          {{ isFoundMode ? '拾金不昧，助失主寻回物品。' : '发布失物信息，增加找回几率。' }}
        </p>
      </div>
    </div>

    <form v-if="isAuthenticated" class="publish-grid" @submit.prevent="submit">
      <label>物品名称 *
        <input v-model="form.itemName" placeholder="请输入物品名称" required />
      </label>

      <label>
        物品分类
        <select v-model="form.category">
          <option value="">请选择分类</option>
          <option v-for="cat in categoriesState.items" :key="cat" :value="cat">{{ cat }}</option>
        </select>
      </label>

      <label>
        所在校区
        <select v-model="form.zone">
          <option value="">请选择校区</option>
          <option value="南门">南门</option>
          <option value="北门">北门</option>
          <option value="东门">东门</option>
          <option value="西门">西门</option>
          <option value="教学楼A">教学楼A</option>
          <option value="教学楼B">教学楼B</option>
          <option value="图书馆">图书馆</option>
          <option value="食堂">食堂</option>
          <option value="操场">操场</option>
          <option value="宿舍区">宿舍区</option>
          <option value="其他">其他</option>
        </select>
      </label>

      <label>
        详细地点
        <input v-model="form.location" placeholder="请输入具体地点，如：图书馆三楼自习室" />
      </label>

      <label>
        {{ isFoundMode ? '拾取时间' : '丢失时间' }}
        <input v-model="form.timeLabel" type="datetime-local" />
      </label>

      <label class="full-span">
        物品描述 *
        <textarea
          v-model="form.description"
          rows="5"
          placeholder="请详细描述物品特征（颜色、品牌、型号等），便于失主确认"
          required
        ></textarea>
      </label>

      <label>
        联系方式类型
        <select v-model="form.contactType">
          <option value="手机">手机</option>
          <option value="微信">微信</option>
          <option value="QQ">QQ</option>
          <option value="邮箱">邮箱</option>
        </select>
      </label>

      <label>
        联系方式
        <input v-model="form.contactValue" :placeholder="`请输入${form.contactType}或手机号`" />
      </label>

      <label class="full-span">
        图片
        <div class="image-input-row">
          <input v-model="imageInput" placeholder="输入图片URL后点击添加" />
          <button type="button" class="secondary small" @click="addImage">添加链接</button>
        </div>
        <div class="image-upload-row">
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            multiple
            style="display: none"
            @change="handleFileSelect"
          />
          <button type="button" class="upload-btn" :disabled="uploadingImages" @click="triggerFileInput">
            {{ uploadingImages ? '上传中...' : '上传图片' }}
          </button>
          <span class="upload-hint">支持 JPG/PNG/GIF，最大 5MB</span>
        </div>
        <div v-if="form.images.length > 0" class="image-preview-list">
          <div v-for="(img, idx) in form.images" :key="idx" class="image-preview-item">
            <img :src="img" alt="预览图" @error="e => e.target.style.display='none'" />
            <button type="button" class="remove-img" @click="removeImage(idx)">×</button>
          </div>
        </div>
      </label>

      <div v-if="successMsg" class="full-span success-msg">{{ successMsg }}</div>
      <div v-if="errorMsg" class="full-span error-msg">{{ errorMsg }}</div>

      <div class="full-span inline-actions">
        <button type="submit" :disabled="submitting" class="primary-btn">
          {{ submitting ? '提交中...' : '立即发布' }}
        </button>
      </div>
    </form>

    <div v-else class="publish-guest-card">
      <p class="publish-guest-kicker">请先登录</p>
      <h3>游客仅浏览，暂不支持直接发布</h3>
      <p class="section-copy">{{ guestPrompt }}</p>
      <div class="publish-guest-actions">
        <RouterLink :to="{ name: 'login', query: { redirect: route.fullPath } }">去登录</RouterLink>
        <RouterLink :to="{ name: 'register', query: { redirect: route.fullPath } }">注册账号</RouterLink>
        <RouterLink :to="{ name: 'home' }">返回首页</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.page-section {
  max-width: 680px;
  margin: 0 auto;
  padding: 1.5rem;
}

.panel-header {
  margin-bottom: 1.5rem;
}

.eyebrow {
  margin: 0;
  font-size: 0.8rem;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

h2 {
  margin: 0.25rem 0 0.5rem;
  font-size: 1.5rem;
}

.section-copy {
  color: #64748b;
  margin: 0;
}

.publish-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.publish-grid label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #334155;
}

.publish-grid label.full-span {
  grid-column: 1 / -1;
}

.publish-grid input,
.publish-grid select,
.publish-grid textarea {
  padding: 0.6rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  background: #fff;
  transition: border-color 0.15s;
}

.publish-grid input:focus,
.publish-grid select:focus,
.publish-grid textarea:focus {
  outline: none;
  border-color: #3b82f6;
}

.publish-grid textarea {
  resize: vertical;
}

.image-input-row {
  display: flex;
  gap: 0.5rem;
}

.image-input-row input {
  flex: 1;
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.image-preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-img {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
}

.error-msg {
  color: #dc2626;
  font-size: 0.875rem;
  padding: 0.5rem 0;
}

.success-msg {
  color: #16a34a;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
}

.inline-actions {
  display: flex;
  gap: 0.75rem;
  padding-top: 0.5rem;
}

.primary-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  flex: 1;
  transition: background 0.15s;
}

.primary-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.primary-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.secondary {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
  padding: 0.6rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  cursor: pointer;
}

.secondary.small {
  padding: 0.5rem 0.75rem;
  font-size: 0.8rem;
}

.publish-guest-card {
  display: grid;
  gap: 1rem;
  padding: 1.5rem;
  border: 1px dashed rgba(59, 130, 246, 0.35);
  border-radius: 1.25rem;
  background: rgba(255, 255, 255, 0.9);
}

.publish-guest-card h3 {
  margin: 0;
  font-size: 1.125rem;
  color: #0f172a;
}

.publish-guest-kicker {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 700;
  color: #2563eb;
}

.publish-guest-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.publish-guest-actions a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.7rem 1rem;
  border-radius: 999px;
  border: 1px solid rgba(37, 99, 235, 0.2);
  color: #1d4ed8;
  background: rgba(239, 246, 255, 0.95);
  font-weight: 600;
  text-decoration: none;
}

.image-upload-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.upload-btn {
  padding: 0.5rem 1rem;
  background: #10b981;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.upload-btn:hover:not(:disabled) {
  background: #059669;
}

.upload-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.upload-hint {
  font-size: 0.75rem;
  color: #94a3b8;
}
</style>
