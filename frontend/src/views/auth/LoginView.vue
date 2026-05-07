<script setup>
import { reactive, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import NotificationToast from '@/components/common/NotificationToast.vue'
import FormInput from '@/components/common/FormInput.vue'
import SlidePuzzleCaptcha from '@/components/common/SlidePuzzleCaptcha.vue'

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

// 验证码状态
const captchaToken = ref('')
const captchaVerified = ref(false)
const showCaptcha = ref(false)

// 通知状态
const notification = reactive({
  show: false,
  type: 'info',
  message: ''
})

const showNotification = (type, message) => {
  notification.type = type
  notification.message = message
  notification.show = true
}

// 验证码回调
function handleCaptchaVerify(token) {
  captchaToken.value = token
  captchaVerified.value = true
  // 立即关闭验证码弹窗，不阻挡后续登录流程
  showCaptcha.value = false
  // 显示验证成功提示
  showNotification('success', '验证成功，正在登录...')
  performLogin()
}

function handleCaptchaError() {
  showNotification('error', '验证码验证失败，请重试')
}

function handleCaptchaExpired() {
  captchaVerified.value = false
  showNotification('warning', '验证码已过期，请重新验证')
}

// 实际执行登录
async function performLogin() {
  try {
    state.loading = true
    const session = await login({
      account: form.account,
      password: form.password,
      captchaToken: captchaToken.value,
    })

    showNotification('success', `登录成功，欢迎 ${session.displayName || form.account}。`)

    // 关闭验证码弹窗
    showCaptcha.value = false

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

  // 显示验证码弹窗
  showCaptcha.value = true
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
        <div class="form-item form-item-link">
          <RouterLink :to="{ name: 'forgot-password' }" class="forgot-link">忘记密码？</RouterLink>
        </div>
        <div class="form-item">
          <button class="login-btn" type="submit" :disabled="state.loading">
            {{ state.loading ? '登录中...' : '登录并进入系统' }}
          </button>
        </div>
      </div>
    </form>

    <!-- 验证码弹窗 -->
    <div v-if="showCaptcha" class="captcha-modal">
      <div class="captcha-overlay" @click="showCaptcha = false"></div>
      <div class="captcha-dialog">
        <h3>安全验证</h3>
        <p class="captcha-hint">请完成以下验证以继续登录</p>
        <SlidePuzzleCaptcha
          @verify="handleCaptchaVerify"
          @error="handleCaptchaError"
          @expired="handleCaptchaExpired"
        />
        <button class="captcha-close-btn" @click="showCaptcha = false">取消</button>
      </div>
    </div>

  </section>
</template>

<style scoped>
.form-item-link {
  display: flex;
  justify-content: flex-end;
}

.forgot-link {
  font-size: 0.875rem;
  color: #6366f1;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}
</style>
