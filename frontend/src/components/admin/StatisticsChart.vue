<script setup>
import { computed, onMounted, ref } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
})

const pieRef = ref(null)
const barRef = ref(null)
const lineRef = ref(null)
let pieChart = null
let barChart = null
let lineChart = null

// 失物与招领分布饼图
const pieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)',
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    textStyle: { color: '#666' },
  },
  series: [
    {
      name: '信息分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2,
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' },
      },
      data: [
        { value: props.data.lostCount, name: '失物信息', itemStyle: { color: '#FF6B6B' } },
        { value: props.data.foundCount, name: '招领信息', itemStyle: { color: '#4ECDC4' } },
      ],
    },
  ],
}))

// 审核处理情况柱状图
const barOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: ['待审核失物', '待审核招领', '待审核认领', '已完成认领', '已拒绝'],
    axisLabel: { color: '#666' },
  },
  yAxis: { type: 'value', axisLabel: { color: '#666' } },
  series: [
    {
      name: '数量',
      type: 'bar',
      data: [
        props.data.pendingLost,
        props.data.pendingFound,
        props.data.pendingClaim,
        props.data.completedClaims,
        props.data.rejectedClaims,
      ],
      itemStyle: {
        color: (params) => ['#FFA726', '#66BB6A', '#42A5F5', '#26A69A', '#EF5350'][params.dataIndex],
        borderRadius: [4, 4, 0, 0],
      },
    },
  ],
}))

// 用户发布趋势折线图
const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: {
    data: ['失物发布', '招领发布'],
    bottom: 0,
    textStyle: { color: '#666' },
  },
  xAxis: {
    type: 'category',
    data: props.data.trendDays,
    axisLabel: { color: '#666' },
  },
  yAxis: { type: 'value', axisLabel: { color: '#666' } },
  series: [
    {
      name: '失物发布',
      type: 'line',
      smooth: true,
      data: props.data.lostTrend,
      areaStyle: { color: 'rgba(255,107,107,0.2)' },
      lineStyle: { color: '#FF6B6B' },
      itemStyle: { color: '#FF6B6B' },
    },
    {
      name: '招领发布',
      type: 'line',
      smooth: true,
      data: props.data.foundTrend,
      areaStyle: { color: 'rgba(78,205,196,0.2)' },
      lineStyle: { color: '#4ECDC4' },
      itemStyle: { color: '#4ECDC4' },
    },
  ],
}))

function initCharts() {
  if (pieRef.value) {
    pieChart = echarts.init(pieRef.value)
    pieChart.setOption(pieOption.value)
  }
  if (barRef.value) {
    barChart = echarts.init(barRef.value)
    barChart.setOption(barOption.value)
  }
  if (lineRef.value) {
    lineChart = echarts.init(lineRef.value)
    lineChart.setOption(lineOption.value)
  }
}

function resizeCharts() {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', resizeCharts)
})
</script>

<template>
  <div class="charts-container">
    <div class="chart-card">
      <h3>信息类型分布</h3>
      <div ref="pieRef" class="chart chart-pie"></div>
    </div>
    <div class="chart-card">
      <h3>审核处理情况</h3>
      <div ref="barRef" class="chart chart-bar"></div>
    </div>
    <div class="chart-card chart-wide">
      <h3>发布趋势</h3>
      <div ref="lineRef" class="chart chart-line"></div>
    </div>
  </div>
</template>

<style scoped>
.charts-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-card h3 {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.chart-wide {
  grid-column: span 2;
}

.chart {
  height: 260px;
}

.chart-pie,
.chart-bar {
  height: 280px;
}

.chart-line {
  height: 240px;
}

@media (max-width: 768px) {
  .charts-container {
    grid-template-columns: 1fr;
  }

  .chart-wide {
    grid-column: span 1;
  }
}
</style>
