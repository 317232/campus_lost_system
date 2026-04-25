<script setup>
import { computed } from 'vue'
import { itemApi } from '@/api/modules'
import ItemCard from '@/components/business/ItemCard.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const foundState = useRemoteCollection(() => itemApi.getItems('found'), foundItems, {
  select: (response) => response?.records || [],
})

const displayItems = computed(() =>
  foundState.items.map((item) => ({
    ...item,
    type: 'found',
    status: item.status === 'PUBLISHED' ? 'published' : item.status?.toLowerCase?.() || 'pending',
  })),
)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Found Items</p>
        <h2>招领大厅</h2>
      </div>
      <div class="filter-row">
        <input placeholder="按物品名称搜索" />
        <!-- 后端传分类，下拉显示 -->
         <!-- 证件， 电子产品，书籍资料，钥匙卡证，衣物配饰，其他 -->
        <select><option >全部分类</option></select>
        <!-- 地点筛选，下拉显示 -->
        <select><option>按地点筛选</option></select>
      </div>
    </div>
    <p v-if="foundState.error" class="feedback feedback-error">招领功能不可用，请联系管理员。</p>

    <div class="card-grid">
      <ItemCard v-for="item in displayItems" :key="item.id" :item="item" />
    </div>
  </section>
</template>
