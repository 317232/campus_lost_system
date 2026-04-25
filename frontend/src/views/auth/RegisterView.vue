<script setup>
import { reactive, ref } from 'vue'
import { authApi } from '@/api/modules/auth'
import NotificationToast from '@/components/common/NotificationToast.vue'
import FormInput from '@/components/common/FormInput.vue'

const form = reactive({
  name: '',
  studentNo: '',
  phone: '',
  email: '',
  emailCode: '',
  password: '',
})

const countdown = ref(0)
let timer = null

const state = reactive({
  loading: false,
  codeLoading: false,
})

const notification = reactive({
  show: false,
  type: '',
  message: '',
})
// 显示通知的函数
const showNotification = (type, message) => {
  notification.type = type
  notification.message = message
  notification.show = true
}

const handleSendCode = async () => {
  if (!form.email) {
    showNotification('error', '请先输入邮箱地址')
    return
  }
  // simple email validation
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    showNotification('error', '请输入有效的邮箱地址')
    return
  }

  try {
    state.codeLoading = true
    await authApi.sendEmailCode({ email: form.email })
    showNotification('success', '验证码发送成功，请查收邮箱')

    // start countdown
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    showNotification('error', error instanceof Error ? error.message : '发送验证码失败，请稍后重试')
  } finally {
    state.codeLoading = false
  }
}

async function handleSubmit() {
  // 逐一进行表单验证
  if (!form.name) {
    showNotification('error', '请输入姓名/昵称');
    return;
  }
  if (form.name.length < 2) {
    showNotification('error', '姓名/昵称过短，至少需要2个字符');
    return;
  }
  if (form.name.length > 20) {
    showNotification('error', '姓名/昵称过长，最多20个字符');
    return;
  }

  if (!form.studentNo) {
    showNotification('error', '请输入学号');
    return;
  }
  if (!/^\d{4,20}$/.test(form.studentNo)) {
    showNotification('error', '学号格式不正确，请填写4-20位数字');
    return;
  }

  if (!form.phone) {
    showNotification('error', '请输入手机号');
    return;
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    showNotification('error', '手机号格式不正确，请填写有效的手机号');
    return;
  }

  if (!form.email) {
    showNotification('error', '请输入邮箱');
    return;
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(form.email)) {
    showNotification('error', '邮箱格式不正确，请填写有效的邮箱');
    return;
  }

  if (!form.emailCode) {
    showNotification('error', '请输入邮箱验证码');
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
    state.loading = true;
    await authApi.register({ ...form })
    showNotification('success', '注册成功，可以使用学号、手机号或邮箱登录。')
  } catch (error) {
    const msg = error instanceof Error ? error.message : String(error);
    if (msg.includes('Duplicate') || msg.includes('重复') || msg.includes('已存在') || msg.includes('已注册')) {
      showNotification('error', '该账号/学号/邮箱/手机号已被他人注册');
    } else {
      showNotification('error', msg || '注册失败，请稍后重试。')
    }
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <section class="auth-card">

    <NotificationToast :show="notification.show" :type="notification.type" :message="notification.message"
      @update:show="(val) => notification.show = val" />

    <div class="auth-card-header">
      <p class="eyebrow">Campus Lost/Find System Login</p>
      <h2>欢迎注册一个新账号</h2>
    </div>

    <form class="stack-form" @submit.prevent="handleSubmit">

      <div class="form_sub">
        <FormInput label="姓名/昵称" v-model="form.name" placeholder="请输入姓名/昵称" />
        <FormInput label="学号" v-model="form.studentNo" placeholder="请输入学号" />
        <FormInput label="手机号" v-model="form.phone" placeholder="请输入手机号" />
        <FormInput type="email" label="邮箱" v-model="form.email" placeholder="请输入邮箱" />
        <FormInput label="邮箱验证码" v-model="form.emailCode" placeholder="请输入邮箱验证码"
          wrapperStyle="display: flex; gap: 10px;">
          <template #append>
            <button type="button" class="register-btn" style="width: 120px; padding: 0; font-size: 14px;"
              @click="handleSendCode" :disabled="countdown > 0 || state.codeLoading">
              {{ countdown > 0 ? `${countdown}s 后重新发送` : (state.codeLoading ? '发送中...' : '获取验证码') }}
            </button>
          </template>
        </FormInput>
        <FormInput type="password" label="密码" v-model="form.password" placeholder="请输入密码" />

        <div class="form-item">
          <button class="register-btn" type="submit" :disabled="state.loading">
            {{ state.loading ? '注册中...' : '提交注册' }}
          </button>
        </div>
      </div>
    </form>

  </section>
</template>
