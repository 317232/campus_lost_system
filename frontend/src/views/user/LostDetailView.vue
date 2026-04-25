<script setup>
import { ref, computed } from 'vue'
import { itemApi } from '@/api/modules'
import { useRoute, useRouter } from 'vue-router'
import StatusBadge from '@/components/common/StatusBadge.vue'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { lostItems } from '../../data/catalog'

const route = useRoute()
const router = useRouter()
const detailState = useRemoteCollection(() => itemApi.getLostDetail(route.params.id), lostItems, {
  select: (item) => [item || lostItems[0]],
})
const item = computed(() => detailState.items[0] || lostItems[0])

const contactUnlocked = ref(false)
const unlockingContact = ref(false)
const contactError = ref('')

async function unlockContact() {
  if (!item.value?.id || item.value.id > 1000000) return
  unlockingContact.value = true
  contactError.value = ''
  try {
    const resp = await itemApi.unlockContact(item.value.id, 'DETAIL_PAGE')
    item.value.contact = resp.contactValue || resp
    contactUnlocked.value = true
  } catch (e) {
    contactError.value = e?.message || '解锁失败'
  } finally {
    unlockingContact.value = false
  }
}

function goReport() {
  router.push({ name: 'report', query: { targetType: 'ITEM', targetId: route.params.id } })
}
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
      <div>
        <dt>联系方式</dt>
        <dd>
          <span v-if="item.id > 1000000">{{ item.contact }}</span>
          <template v-else>
            <template v-if="contactUnlocked">{{ item.contact }}</template>
            <button v-else type="button" class="unlock-btn" :disabled="unlockingContact" @click="unlockContact">
              {{ unlockingContact ? '解锁中...' : '点击解锁' }}
            </button>
          </template>
          <span v-if="contactError" class="error-text">{{ contactError }}</span>
        </dd>
      </div>
      <div><dt>发布人</dt><dd>{{ item.owner }}</dd></div>
    </dl>
    <div class="inline-actions">
      <button type="button">我捡到了</button>
      <button type="button" class="secondary" @click="goReport">举报信息</button>
    </div>
  </section>
</template>

<style scoped>
.inline-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.unlock-btn {
  background: #10b981;
  color: #fff;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
}

.unlock-btn:hover:not(:disabled) {
  background: #059669;
}

.unlock-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.error-text {
  color: #ef4444;
  font-size: 0.75rem;
  margin-left: 0.5rem;
}
</style>
