<script setup>
import { adminApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { notices } from '@/data/catalog'

const noticesState = useRemoteCollection(() => adminApi.getNotices(), notices)
</script>

<template>
  <section class="section-grid">
    <article class="panel">
      <div class="panel-header">
        <div>
          <p class="eyebrow">Notice CRUD</p>
          <h2>公告管理</h2>
        </div>
      </div>
      <form class="stack-form">
        <label>公告标题<input placeholder="请输入公告标题" /></label>
        <label>公告内容<textarea rows="5" placeholder="请输入公告内容"></textarea></label>
        <button type="button">发布公告</button>
      </form>
    </article>

    <article class="panel">
      <div class="panel-header">
        <div>
          <p class="eyebrow">Published Notices</p>
          <h2>已发布公告</h2>
        </div>
      </div>
      <p v-if="noticesState.error" class="feedback feedback-error">公告接口不可用，当前显示演示数据。</p>
      <ul class="panel-list compact-list">
        <li v-for="notice in noticesState.items" :key="notice.id">{{ notice.title }}</li>
      </ul>
    </article>
  </section>
</template>
