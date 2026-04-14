<script setup>
import { reactive } from 'vue'
import { mockMode } from '@/api'
import { authApi } from '@/api/modules/auth'

const form = reactive({
  account: '',
  studentNo: '',
  identityValue: '',
  newPassword: '',
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
    await authApi.forgotPassword({
      account: form.account,
      studentNo: form.studentNo,
      identityValue: form.identityValue,
    })

    await authApi.resetPassword({
      account: form.account,
      newPassword: form.newPassword,
    })

    state.success = mockMode
      ? '当前为 mock 模式，已模拟完成身份核验与密码重置。'
      : '身份核验通过，密码已重置。请使用新密码重新登录。'
  } catch (error) {
    state.error = error instanceof Error ? error.message : '重置失败，请稍后重试。'
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <section class="auth-card">
    <p class="eyebrow">Local Verification</p>
    <h2>忘记密码</h2>
    <form class="stack-form" @submit.prevent="handleSubmit">
      <label>账号<input v-model="form.account" placeholder="请输入学号 / 手机号 / 邮箱" /></label>
      <label>学号<input v-model="form.studentNo" placeholder="核验学号" /></label>
      <label>手机号或邮箱<input v-model="form.identityValue" placeholder="核验绑定信息" /></label>
      <label>新密码<input v-model="form.newPassword" type="password" placeholder="设置新密码" /></label>
      <button type="submit" :disabled="state.loading">
        {{ state.loading ? '重置中...' : '验证并重置密码' }}
      </button>
    </form>
    <p v-if="state.error" class="feedback feedback-error">{{ state.error }}</p>
    <p v-if="state.success" class="feedback feedback-success">{{ state.success }}</p>
  </section>
</template>
