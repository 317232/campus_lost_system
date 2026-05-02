<template>
  <div class="match-recommendations" v-if="matches.length > 0">
    <div class="match-header">
      <span class="match-icon">🔗</span>
      <h3>可能匹配到 {{ scene === 'lost' ? '招领' : '失物' }}信息</h3>
    </div>
    <div class="match-list">
      <div 
        v-for="item in matches" 
        :key="item.id" 
        class="match-card"
        @click="goToDetail(item.id)"
      >
        <div class="match-content">
          <div class="match-title">{{ item.title || item.itemName }}</div>
          <div class="match-info">
            <span class="match-zone" v-if="item.zone">{{ item.zone }}</span>
            <span class="match-location" v-if="item.location">{{ item.location }}</span>
          </div>
          <div class="match-time" v-if="item.timeLabel">{{ item.timeLabel }}</div>
        </div>
        <div class="match-score" :class="getScoreClass(item.matchScore)">
          {{ item.matchScore }}分
        </div>
      </div>
    </div>
    <div class="match-tip">点击卡片查看详情并联系对方</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi } from '@/api/modules/items'

const props = defineProps({
  itemId: {
    type: Number,
    required: true
  },
  scene: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const matches = ref([])
const matchScene = ref('')

const loadMatches = async () => {
  try {
    const result = await itemApi.getMatches(props.itemId, 3)
    if (result && result.items) {
      matches.value = result.items
      matchScene.value = result.scene || ''
    }
  } catch (error) {
    console.error('加载匹配推荐失败:', error)
  }
}

const goToDetail = (id) => {
  const targetScene = props.scene === 'lost' ? 'found' : 'lost'
  router.push(`/${targetScene === 'lost' ? 'lost' : 'found'}/detail/${id}`)
}

const getScoreClass = (score) => {
  if (score >= 70) return 'score-high'
  if (score >= 50) return 'score-medium'
  return 'score-low'
}

onMounted(() => {
  loadMatches()
})
</script>

<style scoped>
.match-recommendations {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  margin: 20px 0;
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.match-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.match-icon {
  font-size: 24px;
}

.match-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.match-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.match-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
}

.match-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.match-content {
  flex: 1;
  min-width: 0;
}

.match-title {
  font-weight: 600;
  font-size: 15px;
  color: #1a1a1a;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.match-info {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.match-zone {
  background: #e8f4ff;
  color: #007aff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.match-time {
  font-size: 12px;
  color: #999;
}

.match-score {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  min-width: 60px;
  text-align: center;
}

.score-high {
  background: linear-gradient(135deg, #4ade80, #22c55e);
  color: white;
}

.score-medium {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
}

.score-low {
  background: #e5e7eb;
  color: #666;
}

.match-tip {
  text-align: center;
  font-size: 12px;
  opacity: 0.8;
  margin-top: 12px;
}
</style>
