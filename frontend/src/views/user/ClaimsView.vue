<script setup>
import { claimApi } from '@/api/modules'
import { useRemoteCollection } from '@/composables/useRemoteCollection'
import { claims } from '../../data/catalog'

const claimState = useRemoteCollection(() => claimApi.listMine(), claims)
</script>

<template>
  <section class="page-section">
    <div class="panel-header">
      <div>
        <p class="eyebrow">Claim Tracking</p>
        <h2>认领进度</h2>
        
      </div>
    </div>
    <p v-if="claimState.error" class="feedback feedback-error">认领接口不可用，已回退到演示数据。</p>

    <div class="panel-list claims-list">
      <article v-for="claim in claimState.items" :key="claim.id" class="claim-card">
        <strong>{{ claim.itemTitle }}</strong>
        <span>{{ claim.stage || claim.status }}</span>
        <p>{{ claim.proof }}</p>
        <small>{{ claim.updatedAt }}</small>
      </article>
    </div>
  </section>
</template>
