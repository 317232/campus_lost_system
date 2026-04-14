<script setup>
import { noticeApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { notices } from '../../data/catalog'

const noticeState = useRemoteCollection(() => noticeApi.list(), notices)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Notice Feed</p>
        <h2>平台公告</h2>
      </div>
    </div>
    <p v-if="noticeState.error" class="feedback feedback-error">公告接口不可用，已回退到演示数据。</p>
    <div class="panel-list">
      <article v-for="notice in noticeState.items" :key="notice.id" class="notice-card">
        <strong>{{ notice.title }}</strong>
        <p>{{ notice.summary || notice.content }}</p>
        <small>{{ notice.publishedAt }}</small>
      </article>
    </div>
  </section>
</template>
