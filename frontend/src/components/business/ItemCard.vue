<script setup>
import { computed } from 'vue'
import StatusBadge from '../common/StatusBadge.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
})

const targetName = props.item.type === 'found' ? 'found-detail' : 'lost-detail'

const visualMap = {
  证件: '证',
  电子产品: '电',
  书籍资料: '书',
  钥匙卡证: '钥',
  衣物配饰: '衣',
  其他: '物',
}

const visualBadge = computed(() => visualMap[props.item.category] || '寻')

const hasImage = computed(() => props.item.image && props.item.image.trim() !== '')
</script>

<template>
  <article class="item-card market-item-card">
    <div class="market-item-media" :data-category="props.item.category">
      <img v-if="hasImage" :src="props.item.image" :alt="props.item.title" class="item-img" />
      <div v-else class="item-icon-badge">{{ visualBadge }}</div>
    </div>

    <div class="card-body">
      <div class="card-topline">
        <StatusBadge :status="props.item.status" />
        <span class="item-category">{{ props.item.category }}</span>
      </div>
      <div class="card-content">
        <h3>{{ props.item.title }}</h3>
        <p class="item-desc">{{ props.item.description }}</p>
        <dl class="item-info">
          <div><dt>地点:</dt><dd>{{ props.item.location }}</dd></div>
          <div><dt>时间:</dt><dd>{{ props.item.time }}</dd></div>
          <div><dt>联系人:</dt><dd>{{ props.item.contact }}</dd></div>
        </dl>
      </div>
    </div>

    <div class="market-item-footer">
      <div class="market-item-meta">
        <strong>{{ props.item.itemName || props.item.title }}</strong>
        <span>{{ props.item.owner || '平台登记' }}</span>
      </div>
      <RouterLink class="card-link market-card-cta" :to="{ name: targetName, params: { id: props.item.id } }">
        查看详情
      </RouterLink>
    </div>
  </article>
</template>
