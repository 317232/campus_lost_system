<script setup>
import { computed } from 'vue'
import { itemApi } from '@/api/modules'
import { useRoute } from 'vue-router'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { lostItems } from '../../data/catalog'

const route = useRoute()
const detailState = useRemoteCollection(() => itemApi.getLostDetail(route.params.id), lostItems, {
  select: (item) => [item || lostItems[0]],
})
const item = computed(() => detailState.items[0] || lostItems[0])
</script>

<template>
  <section class="detail-card">
    <div class="card-topline">
      <StatusBadge :status="item.status" />
      <span>{{ item.category }}</span>
    </div>
    <h2>{{ item.title }}</h2>
    <p class="section-copy">{{ item.description }}</p>
    <dl class="detail-grid">
      <div><dt>丢失地点</dt><dd>{{ item.location }}</dd></div>
      <div><dt>丢失时间</dt><dd>{{ item.time }}</dd></div>
      <div><dt>联系人</dt><dd>{{ item.contact }}</dd></div>
      <div><dt>发布人</dt><dd>{{ item.owner }}</dd></div>
    </dl>
  </section>
</template>
