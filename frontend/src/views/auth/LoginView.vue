<script setup>
import { reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { mockMode } from '@/api'
import { useAuth } from '@/composables/useAuth'

const form = reactive({
  account: '',
  password: '',
  captcha: '',
})

const route = useRoute()
const router = useRouter()
const { login } = useAuth()
const state = reactive({
  loading: false,
  error: '',
  success: '',
})

async function handleSubmit() {
  state.loading = true
  state.error = ''
  state.success = ''

  try {
    const session = await login({
      account: form.account,
      password: form.password,
      captcha: form.captcha,
    })

    state.success = mockMode
      ? '当前为 mock 登录，页面状态已写入本地。'
      : `登录成功，欢迎 ${session.displayName || form.account}。`

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
    state.error = error instanceof Error ? error.message : '登录失败，请稍后重试。'
  } finally {
    state.loading = false
  }
}

async function handleTestLogin() {
  form.account = 'admin001'
  form.password = 'admin123'
  await handleSubmit()
}
</script>

<template>
  <section class="auth-card">
    <div>
      <p class="eyebrow">JWT Login</p>
      <h2>登录校园失物招领平台</h2>
      <p class="section-copy">支持学号、手机号或邮箱登录。连续失败 3 次后显示图形验证码。</p>
    </div>

    <form class="stack-form" @submit.prevent="handleSubmit">
      <label>
        账号
        <input v-model="form.account" placeholder="学号 / 手机号 / 邮箱" />
      </label>
      <label>
        密码
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </label>
      <label>
        验证码
        <input v-model="form.captcha" placeholder="第三次失败后启用" />
      </label>
      <button type="button" @click="handleTestLogin">使用测试数据1</button>
      <button type="submit" :disabled="state.loading">
        {{ state.loading ? '登录中...' : '登录并进入系统' }}
      </button>
    </form>

    <p v-if="state.error" class="feedback feedback-error">{{ state.error }}</p>
    <p v-if="state.success" class="feedback feedback-success">{{ state.success }}</p>

    <div class="auth-actions">
      <RouterLink to="/register">注册账号</RouterLink>
      <RouterLink to="/forgot-password">忘记密码</RouterLink>
      <RouterLink to="/">返回首页</RouterLink>
      <RouterLink to="/admin">进入管理员演示</RouterLink>
    </div>
  </section>
</template>
