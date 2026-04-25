<script setup>
import { reactive, ref } from 'vue'
import { authApi } from '@/api/modules/auth'
import NotificationToast from '@/components/common/NotificationToast.vue'
import FormInput from '@/components/common/FormInput.vue'

const form = reactive({
  account: '',
  verifyCode: '',
  newPassword: '',
})

const state = reactive({
  loading: false,
  codeLoading: false,
})

const countdown = ref(0)
let timer = null
// 通知状态
const notification = reactive({
  show: false,
  type: 'info', // 'success', 'error', 'info', 'warning'
  message: ''
})

// 显示通知的函数
const showNotification = (type, message) => {
  notification.type = type
  notification.message = message
  notification.show = true
}

const handleSendCode = async () => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.account) {
    // state.error = '请先输入账号'
    showNotification('error', '请先输入邮箱地址')
    return
  }
  if (!emailRegex.test(form.account)) {
    // state.error = '请输入有效的邮箱地址'
    showNotification('error', '请输入有效的邮箱地址')
    return
  }

  try {
    state.codeLoading = true
    await authApi.forgotPassword({ account: form.account })
    // state.success = '验证码发送成功，请查收账号绑定的邮箱'
    showNotification('success', '验证码发送成功，请在60秒内输入验证码')
    // start countdown
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    // state.error = error instanceof Error ? error.message : '发送验证码失败，请稍后重试'
    showNotification('error', error instanceof Error ? error.message : '发送验证码失败，请稍后重试')
  } finally {
    state.codeLoading = false
  }
}

async function handleSubmit() {
  if (!form.account) {
    showNotification('error', '请输入邮箱地址')
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.account)) {
    showNotification('error', '邮箱格式不正确，请输入有效的邮箱地址')
    return
  }

  if (!form.verifyCode) {
    showNotification('error', '请输入邮箱验证码')
    return
  }

  if (!form.newPassword) {
    showNotification('error', '请输入新密码')
    return
  }
  if (form.newPassword.length < 6) {
    showNotification('error', '新密码过短，至少需要6个字符')
    return
  }
  if (form.newPassword.length > 20) {
    showNotification('error', '新密码过长，最多20个字符')
    return
  }

  state.loading = true

  try {
    await authApi.resetPassword({
      account: form.account,
      verifyCode: form.verifyCode,
      newPassword: form.newPassword,
    })
    showNotification('success', '身份核验通过，密码已重置。请使用新密码重新登录。')

  } catch (error) {
    showNotification('error', error instanceof Error ? error.message : '重置失败，请稍后重试。')
    // state.error = error instanceof Error ? error.message : '重置失败，请稍后重试。'
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <section class="auth-card">
    <!-- 使用公共通知组件 -->
    <NotificationToast :show="notification.show" :type="notification.type" :message="notification.message"
      @update:show="(val) => notification.show = val" />
    <div class="auth-card-header">
      <p class="eyebrow">Campus Lost/Find System Login</p>
      <h2>找回密码</h2>
    </div>
    <form class="stack-form" @submit.prevent="handleSubmit">

      <div class="form_sub">
        <FormInput label="邮箱" v-model="form.account" placeholder="请输入邮箱" />
        <FormInput label="邮箱验证码" v-model="form.verifyCode" placeholder="请输入邮箱验证码"
          wrapperStyle="display: flex; gap: 10px;">
          <template #append>
            <button type="button" class="register-btn" style="width: 120px; padding: 0; font-size: 14px;"
              @click="handleSendCode" :disabled="countdown > 0 || state.codeLoading">
              {{ countdown > 0 ? `${countdown}s 后重新发送` : (state.codeLoading ? '发送中...' : '获取验证码') }}
            </button>
          </template>
        </FormInput>
        <FormInput type="password" label="新密码" v-model="form.newPassword" placeholder="设置新密码" />

        <div class="form-item">
          <button class="forgot-btn" type="submit" :disabled="state.loading">
            {{ state.loading ? '重置中...' : '验证并重置密码' }}
          </button>
        </div>
      </div>
    </form>
  </section>
</template>
