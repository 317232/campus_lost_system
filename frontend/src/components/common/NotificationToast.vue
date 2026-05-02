<template>
  <!-- 通知弹窗 -->
  <div v-if="show" class="notification-toast" :class="`notification-${type}`" role="alert" aria-live="polite">
    {{ message }}
  </div>
</template>

<script setup>
import { watch } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'error', 'info', 'warning'].includes(value)
  },
  message: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 3000
  }
})

const emit = defineEmits(['update:show'])

// 当show变为true时，启动定时器自动关闭
watch(() => props.show, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      emit('update:show', false)
    }, props.duration)
  }
})
</script>

<style scoped>
.notification-toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 12px;
  color: white;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 300px;
  text-align: center;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    top: -50px;
    opacity: 0;
  }

  to {
    top: 20px;
    opacity: 1;
  }
}

.notification-success {
  background-color: #67c23a;
}

.notification-error {
  background-color: #f2aaaa;
}

.notification-info {
  background-color: #409eff;
}

.notification-warning {
  background-color: #e6a23c;
}
</style>