<script setup>
import { computed } from 'vue'
import { itemApi } from '@/api/modules'
import { useRoute } from 'vue-router'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { foundItems } from '../../data/catalog'

const route = useRoute()
const detailState = useRemoteCollection(() => itemApi.getFoundDetail(route.params.id), foundItems, {
  select: (item) => [item || foundItems[0]],
})
const item = computed(() => detailState.items[0] || foundItems[0])
</script>

<template>
  <section class="detail-card">
    <div class="card-topline">
      <StatusBadge :status="item.status" />
      <span>{{ item.category }}</span>
    </div>
    <h2>{{ item.title }}</h2>
    <!-- 信息描述进行遮挡，点击发起认领后简短的关键词验证，后显示完整信息，后获取地点信息 -->
    <p class="section-copy">描述: {{ item.description }}</p>

    <dl class="detail-grid">
      <!-- 放图片  -->
      <div class="detail-item">
        <div class="detail-item-box">
          <img :src="item.image" alt="物品图片" />
        </div>

        <div class="detail-item-content">
          <dt>拾取地点</dt>
          <dd>{{ item.location }}</dd>
          <dt>拾取时间</dt>
          <dd>{{ item.time }}</dd>
          <dt>联系人</dt>
          <dd>{{ item.contact }}</dd>
          <dt>发布人</dt>
          <dd>{{ item.owner }}</dd>
        </div>
      </div>
    </dl>

    <div class="inline-actions">
      <button type="button">发起认领</button>
      <button type="button" class="secondary">举报信息</button>
      <!-- 表示感谢…… -->
    </div>
  </section>
</template>
