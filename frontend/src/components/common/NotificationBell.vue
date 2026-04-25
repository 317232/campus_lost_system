<template>
  <div class="notification-wrapper">
    <NotificationBell />
  </div>
</template>

<script setup>
import NotificationBell from './NotificationBell.vue'
</script>

<style scoped>
.notification-wrapper {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 999;
}
</style>
