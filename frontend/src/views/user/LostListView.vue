<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { itemApi, categoriesApi } from '@/api/modules'
import ItemCard from '@/components/business/ItemCard.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'

const categoriesState = useRemoteCollection(() => categoriesApi.list(), [])

// Filter state
const filters = reactive({
  keyword: '',
  category: '',
  zone: '',
  sortBy: 'time', // time or relevance
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// Build query params from filters
function buildParams() {
  return {
    scene: 'lost',
    keyword: filters.keyword || undefined,
    category: filters.category || undefined,
    zone: filters.zone || undefined,
    page: pagination.page,
    pageSize: pagination.pageSize,
  }
}

// Use a ref to force reload when params change
const triggerKey = ref(0)
const lostState = useRemoteCollection(
  () => {
    triggerKey.value // dependency
    return itemApi.getItems('lost', buildParams())
  },
  [],
  {
    select: (response) => {
      const records = response?.records || []
      pagination.total = response?.total || 0
      return records
    },
  }
)

// Reload when filters or page change
watch([filters, () => pagination.page], () => {
  triggerKey.value++
  lostState.reload()
}, { deep: true })

function handleSearch() {
  pagination.page = 1
  triggerKey.value++
  lostState.reload()
}

function handlePageChange(newPage) {
  pagination.page = newPage
  lostState.reload()
}

function resetFilters() {
  filters.keyword = ''
  filters.category = ''
  filters.zone = ''
  filters.sortBy = 'time'
  pagination.page = 1
}

const totalPages = computed(() => Math.ceil(pagination.total / pagination.pageSize) || 1)

const displayItems = computed(() =>
  lostState.items.map((item) => ({
    ...item,
    type: 'lost',
    status: item.status === 'PUBLISHED' ? 'urgent' : item.status?.toLowerCase?.() || 'pending',
  })),
)

onMounted(() => {
  categoriesState.reload()
})
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Lost Items</p>
        <h2>失物大厅</h2>
      </div>
      <div class="filter-row">
        <input
          v-model="filters.keyword"
          placeholder="按物品名称搜索"
          @keyup.enter="handleSearch"
        />
        <select v-model="filters.category">
          <option value="">全部分类</option>
          <option v-for="cat in categoriesState.items" :key="cat" :value="cat">{{ cat }}</option>
        </select>
        <select v-model="filters.zone">
          <option value="">按地点筛选</option>
          <option value="南门">南门</option>
          <option value="北门">北门</option>
          <option value="东门">东门</option>
          <option value="西门">西门</option>
          <option value="教学楼A">教学楼A</option>
          <option value="教学楼B">教学楼B</option>
          <option value="图书馆">图书馆</option>
          <option value="食堂">食堂</option>
          <option value="操场">操场</option>
          <option value="宿舍区">宿舍区</option>
          <option value="其他">其他</option>
        </select>
        <button class="filter-btn" @click="handleSearch">搜索</button>
        <button v-if="filters.keyword || filters.category || filters.zone" class="filter-btn reset" @click="resetFilters">重置</button>
      </div>
    </div>
    <p v-if="lostState.error" class="feedback feedback-error">当前网络不可用，请刷新后重试。</p>

    <div class="card-grid">
      <ItemCard v-for="item in displayItems" :key="item.id" :item="item" />
    </div>

    <div v-if="pagination.total > 0" class="pagination">
      <span class="pagination-info">共 {{ pagination.total }} 条</span>
      <button :disabled="pagination.page <= 1" @click="handlePageChange(pagination.page - 1)">上一页</button>
      <span class="pagination-current">{{ pagination.page }} / {{ totalPages }}</span>
      <button :disabled="pagination.page >= totalPages" @click="handlePageChange(pagination.page + 1)">下一页</button>
    </div>
  </section>
</template>

<style scoped>
.filter-row {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
}

.filter-row input {
  padding: 0.5rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
}

.filter-row select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  background: #fff;
}

.filter-btn {
  padding: 0.5rem 1rem;
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  cursor: pointer;
}

.filter-btn.reset {
  background: #94a3b8;
}

.pagination {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  justify-content: center;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.pagination button {
  padding: 0.4rem 0.9rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  font-size: 0.875rem;
  cursor: pointer;
}

.pagination button:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 0.875rem;
  color: #64748b;
}

.pagination-current {
  font-size: 0.875rem;
  color: #334155;
  font-weight: 500;
}
</style>
