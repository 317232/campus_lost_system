<script setup>
import { reactive } from 'vue'
import { RouterLink } from 'vue-router'
import { mockMode } from '@/api'
import { authApi } from '@/api/modules/auth'

const form = reactive({
  name: '',
  studentNo: '',
  phone: '',
  email: '',
  password: '',
})

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
    await authApi.register({ ...form })
    state.success = mockMode
      ? '当前为 mock 模式，已模拟完成注册。'
      : '注册成功，可以使用学号、手机号或邮箱登录。'
  } catch (error) {
    state.error = error instanceof Error ? error.message : '注册失败，请稍后重试。'
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <section class="auth-card">
    <h2>创建普通用户账号</h2>
    <form class="stack-form" @submit.prevent="handleSubmit">
      <label>姓名<input v-model="form.name" placeholder="请输入真实姓名" /></label>
      <label>学号<input v-model="form.studentNo" placeholder="请输入学号" /></label>
      <label>手机号<input v-model="form.phone" placeholder="请输入手机号" /></label>
      <label>邮箱<input v-model="form.email" placeholder="请输入邮箱" /></label>
      <label>密码<input v-model="form.password" type="password" placeholder="请设置密码" /></label>
      <button type="submit" :disabled="state.loading">
        {{ state.loading ? '注册中...' : '提交注册' }}
      </button>
    </form>
    <p v-if="state.error" class="feedback feedback-error">{{ state.error }}</p>
    <p v-if="state.success" class="feedback feedback-success">{{ state.success }}</p>
    <div class="auth-actions">
      <RouterLink to="/login">已有账号，去登录</RouterLink>
      <RouterLink to="/forgot-password">忘记密码</RouterLink>
      <RouterLink to="/">返回首页</RouterLink>
    </div>
  </section>
</template>
