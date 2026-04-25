<script setup>
import { computed } from 'vue'
import { itemApi } from '@/api/modules'
import ItemCard from '@/components/business/ItemCard.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const lostState = useRemoteCollection(() => itemApi.getItems('lost'), lostItems, {
  select: (response) => response?.records || [],
})

const displayItems = computed(() =>
  lostState.items.map((item) => ({
    ...item,
    type: 'lost',
    status: item.status === 'PUBLISHED' ? 'urgent' : item.status?.toLowerCase?.() || 'pending',
  })),
)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Lost Items</p>
        <h2>失物大厅</h2>
      </div>
      <div class="filter-row">
        <input placeholder="按物品名称搜索" />
        <select><option>全部分类</option></select>
        <select><option>按时间排序</option></select>
      </div>
    </div>
    <p v-if="lostState.error" class="feedback feedback-error">失物接口不可用，当前显示演示数据。</p>

    <div class="card-grid">
      <ItemCard v-for="item in displayItems" :key="item.id" :item="item" />
    </div>
  </section>
</template>
