<script setup>
import { computed } from 'vue'
import { claimApi, userApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const profileFallback = [
  {
    id: 1,
    name: '王同学',
    realName: '王同学',
    studentNo: '2023123456',
    phone: '18812341024',
    email: 'demo@campus.edu.cn',
  },
]

const profileState = useRemoteCollection(() => userApi.getProfile(), profileFallback, {
  select: (profile) => (profile ? [profile] : profileFallback),
})
const lostState = useRemoteCollection(() => userApi.getMyLostItems(), [])
const foundState = useRemoteCollection(() => userApi.getMyFoundItems(), [])
const claimState = useRemoteCollection(() => claimApi.listMine(), [])

const profile = computed(() => profileState.items[0] || profileFallback[0])
</script>

<template>
  <section class="section-grid">
    <article class="panel">
      <div class="panel-header">
        <div>
          <p class="eyebrow">Profile</p>
          <h2>个人信息</h2>
        </div>
      </div>
      <form class="stack-form">
        <label>姓名<input :value="profile.realName || profile.name" /></label>
        <label>学号<input :value="profile.studentNo" /></label>
        <label>手机号<input :value="profile.phone" /></label>
        <label>邮箱<input :value="profile.email" /></label>
        <button type="button">保存修改</button>
      </form>
    </article>

    <article class="panel">
      <div class="panel-header">
        <div>
          <p class="eyebrow">My Records</p>
          <h2>个人记录摘要</h2>
        </div>
      </div>
      <ul class="panel-list compact-list">
        <li>我发布的失物: {{ lostState.items.length }} 条</li>
        <li>我发布的招领: {{ foundState.items.length }} 条</li>
        <li>我提交的认领: {{ claimState.items.length }} 条</li>
        <li>待我补充说明: {{ claimState.items.filter((item) => (item.stage || item.status) === '待审核').length }} 条</li>
      </ul>
    </article>
  </section>
</template>
