<script setup>
import { reactive, onMounted, ref, defineEmits, defineProps } from 'vue'

const props = defineProps({
  width: {
    type: Number,
    default: 120
  },
  height: {
    type: Number,
    default: 40
  },
  length: {
    type: Number,
    default: 4
  }
})

const emit = defineEmits(['update:modelValue'])

const captchaRef = ref(null)
const captchaState = reactive({
  value: '',
  canvasWidth: props.width,
  canvasHeight: props.height,
})

// 生成随机验证码
function generateCaptcha() {
  const chars = 'ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789';
  let result = '';
  for (let i = 0; i < props.length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  captchaState.value = result
  drawCaptcha()
}

// 绘制验证码到canvas
function drawCaptcha() {
  const canvas = captchaRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')

  // 清空画布
  ctx.clearRect(0, 0, captchaState.canvasWidth, captchaState.canvasHeight)

  // 设置背景色
  ctx.fillStyle = '#f0f0f0'
  ctx.fillRect(0, 0, captchaState.canvasWidth, captchaState.canvasHeight)

  // 绘制干扰线
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = `rgb(${Math.floor(Math.random() * 255)},${Math.floor(Math.random() * 255)},${Math.floor(Math.random() * 255)})`
    ctx.beginPath()
    ctx.moveTo(Math.random() * captchaState.canvasWidth, Math.random() * captchaState.canvasHeight)
    ctx.lineTo(Math.random() * captchaState.canvasWidth, Math.random() * captchaState.canvasHeight)
    ctx.stroke()
  }

  // 绘制验证码字符
  ctx.font = 'bold 20px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'

  for (let i = 0; i < captchaState.value.length; i++) {
    ctx.fillStyle = `rgb(${Math.floor(Math.random() * 155 + 100)},${Math.floor(Math.random() * 155 + 100)},${Math.floor(Math.random() * 155 + 100)})`
    const x = (i + 0.5) * (captchaState.canvasWidth / props.length)
    const y = captchaState.canvasHeight / 2
    const angle = (Math.random() - 0.5) * 0.5 // 随机角度
    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(angle)
    ctx.fillText(captchaState.value[i], 0, 0)
    ctx.restore()
  }
}

// 刷新验证码
function refreshCaptcha() {
  generateCaptcha()
}

// 获取验证码值（仅供后端验证使用，前端不应存储或传输此值）
function getValue() {
  return captchaState.value
}

// 设置验证码值
function setValue(value) {
  captchaState.value = value
}

// 组件挂载时生成验证码
onMounted(() => {
  generateCaptcha()
})

// 暴露方法给父组件
defineExpose({
  getValue,
  setValue,
  refreshCaptcha
})
</script>

<template>
  <div class="captcha-container">
    <canvas ref="captchaRef" :width="captchaState.canvasWidth" :height="captchaState.canvasHeight"
      @click="refreshCaptcha" title="点击刷新验证码"></canvas>
  </div>
</template>

<style scoped>
.captcha-container {
  display: flex;
  align-items: center;
  gap: 10px;
}


.refresh-captcha-btn {
  padding: 5px 10px;
  background-color: #000000;
  border: 1px solid #ccc;
  cursor: pointer;
  border-radius: 4px;
}

#captchaCanvas {
  margin-top: 10px;
  border: 1px solid #ccc;
  box-shadow: var(--shadow-card);
}
</style>