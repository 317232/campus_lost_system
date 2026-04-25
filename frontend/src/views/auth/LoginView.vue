<script setup>
import { reactive, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import NotificationToast from '@/components/common/NotificationToast.vue'
import FormInput from '@/components/common/FormInput.vue'

const form = reactive({
  account: '',
  password: '',
})

const route = useRoute()
const router = useRouter()
const { login } = useAuth()
const state = reactive({
  loading: false,
})

// 通知状态
const notification = reactive({
  show: false,
  type: 'info',
  message: ''
})

// 显示通知的函数
const showNotification = (type, message) => {
  notification.type = type
  notification.message = message
  notification.show = true
}

// TODO: 登录表单验证规则，优化
async function handleSubmit() {
  if (!form.account) {
    showNotification('error', '请输入登录账号');
    return;
  }
  if (form.account.length < 4) {
    showNotification('error', '账号过短，至少需要4个字符');
    return;
  }
  if (form.account.length > 30) {
    showNotification('error', '账号过长，最多30个字符');
    return;
  }

  if (!form.password) {
    showNotification('error', '请输入密码');
    return;
  }
  if (form.password.length < 6) {
    showNotification('error', '密码过短，至少需要6个字符');
    return;
  }
  if (form.password.length > 20) {
    showNotification('error', '密码过长，最多20个字符');
    return;
  }

  try {
    state.loading = true
    const session = await login({
      account: form.account,
      password: form.password,
    })

    showNotification('success', `登录成功，欢迎 ${session.displayName || form.account}。`)

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''

    if (redirect) {
      router.push(redirect)
      return
    }

    if (session.role === 'ADMIN') {
      router.push('/admin')
      return
    }

    router.push('/')
  } catch (error) {
    showNotification('error', error instanceof Error ? error.message : '登录失败，请稍后重试。')
  } finally {
    state.loading = false
  }
}

async function handleTestLogin() {
  form.account = '2024001'
  form.password = 'password123'
  await handleSubmit()
}
</script>

<template>
  <section class="auth-card">
    <!-- 使用公共通知组件 -->
    <NotificationToast :show="notification.show" :type="notification.type" :message="notification.message"
      @update:show="(val) => notification.show = val" />

    <div class="auth-card-header">
      <p class="eyebrow">Campus Lost/Find System Login</p>
      <h2>登录校园失物招领平台</h2>
    </div>

    <form class="stack-form" @submit.prevent="handleSubmit">

      <div class="form_sub">
        <FormInput label="登录账号" v-model="form.account" placeholder="学号 / 手机号 / 邮箱" />
        <FormInput type="password" label="密码" v-model="form.password" placeholder="请输入密码" />
        <div class="form-item">
          <button class="login-btn" type="button" @click="handleTestLogin">使用测试数据</button>
          <button class="login-btn" type="submit" :disabled="state.loading">
            {{ state.loading ? '登录中...' : '登录并进入系统' }}
          </button>
        </div>
      </div>
    </form>

    <div class="auth-actions">
      <RouterLink to="/register">注册账号</RouterLink>
      <RouterLink to="/forgot-password">忘记密码</RouterLink>
      <RouterLink to="/">返回首页</RouterLink>
    </div>

  </section>
</template>
