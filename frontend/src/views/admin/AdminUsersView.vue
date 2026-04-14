<script setup>
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const fallbackUsers = [
  { id: 1, studentNo: '2023123456', role: 'USER', status: 'NORMAL' },
  { id: 2, studentNo: 'admin001', role: 'ADMIN', status: 'NORMAL' },
]

const usersState = useRemoteCollection(() => adminApi.getUsers(), fallbackUsers)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Role Control</p>
        <h2>用户管理</h2>
      </div>
    </div>
    <div class="table-shell">
      <table>
        <thead>
          <tr><th>账号</th><th>角色</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="user in usersState.items" :key="user.id">
            <td>{{ user.studentNo || user.username }}</td>
            <td>{{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}</td>
            <td>{{ user.status || 'NORMAL' }}</td>
            <td>{{ user.role === 'ADMIN' ? '查看权限' : '禁用 / 重置密码' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
