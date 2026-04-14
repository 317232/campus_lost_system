<script setup>
const props = defineProps({
  status: {
    type: String,
    default: 'pending',
  },
})

const labelMap = {
  urgent: '高优先',
  pending: '待处理',
  claimed: '已认领',
  value: '关键件',
}
</script>

<template>
  <span class="status-badge" :data-status="props.status">
    {{ labelMap[props.status] || props.status }}
  </span>
</template>
