<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">统计看板</h1>
        <p class="muted">展示工单总量、待处理、已完成和完成率，后续由真实统计 API 驱动。</p>
      </div>
      <el-button :loading="loading" @click="loadOverview">刷新</el-button>
    </div>

    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :xs="24" :sm="12" :lg="6">
        <div class="metric panel">
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
        </div>
      </el-col>
    </el-row>

    <div class="panel placeholder">
      <el-empty description="图表区域待统计模块接入 ECharts" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getOverview, type Overview } from '@/api/systemApi'

const loading = ref(false)
const overview = ref<Overview>({
  totalOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  completionRate: 0,
})

const cards = computed(() => [
  { label: '工单总数', value: overview.value.totalOrders },
  { label: '待处理', value: overview.value.pendingOrders },
  { label: '已完成', value: overview.value.completedOrders },
  { label: '完成率', value: `${overview.value.completionRate}%` },
])

async function loadOverview() {
  loading.value = true
  try {
    const response = await getOverview()
    overview.value = response.data.data
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.metric {
  display: flex;
  min-height: 110px;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 18px;
}

.metric span {
  color: var(--cr-muted);
  font-size: 14px;
}

.metric strong {
  color: var(--cr-deep-blue);
  font-size: 30px;
}

.placeholder {
  min-height: 320px;
  padding: 24px;
}
</style>
