<template>
  <div class="page">
    <el-skeleton :loading="loading" animated :rows="8">
      <div class="grid" v-if="essay">
        <section class="card">
          <h1>{{ essay.taskTitle }}</h1>
          <p class="muted">{{ essay.taskType }} · {{ essay.wordCount }} 词 · {{ essay.status }}</p>
          <p>{{ essay.taskDescription }}</p>
          <pre>{{ essay.content }}</pre>
          <el-button type="primary" :loading="reviewing" @click="runReview">AI 批改</el-button>
        </section>
        <section class="card" v-if="review">
          <div class="score">
            <div>
              <p class="muted">总分</p>
              <strong class="big">{{ review.overallScore }}</strong>
            </div>
            <div ref="chartEl" class="chart" role="img" aria-label="四维分数雷达图"></div>
          </div>
          <p>{{ review.comment }}</p>
          <h2>逐句建议</h2>
          <article v-for="(item, index) in review.feedback" :key="index" class="feedback">
            <p><strong>原文</strong> {{ item.original }}</p>
            <p><strong>建议</strong> {{ item.suggestion }}</p>
            <p class="muted">{{ item.reason }}</p>
          </article>
        </section>
      </div>
    </el-skeleton>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import type { ECharts } from 'echarts'
import { writingApi } from '../api'
import type { EssayDetail, EssayReview } from '../types'

const route = useRoute()
const essay = ref<EssayDetail>()
const review = ref<EssayReview>()
const loading = ref(true)
const reviewing = ref(false)
const chartEl = ref<HTMLDivElement>()
let chart: ECharts | null = null

const essayId = Number(route.params.essayId)

async function load() {
  loading.value = true
  try {
    const res = await writingApi.essay(essayId)
    essay.value = res.data
    review.value = res.data.review
  } finally {
    loading.value = false
  }
}

async function runReview() {
  reviewing.value = true
  try {
    const res = await writingApi.review(essayId)
    review.value = res.data
  } finally {
    reviewing.value = false
  }
}

async function renderChart() {
  if (!chartEl.value || !review.value) return
  const echarts = await import('echarts')
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  chart.setOption({
    radar: {
      indicator: [
        { name: 'TA', max: 9 },
        { name: 'CC', max: 9 },
        { name: 'LR', max: 9 },
        { name: 'GRA', max: 9 },
      ],
    },
    series: [{
      type: 'radar',
      data: [{
        value: [review.value.taScore, review.value.ccScore, review.value.lrScore, review.value.graScore],
        name: 'Scores',
      }],
      areaStyle: { opacity: 0.2 },
      color: '#4F46E5',
    }],
  })
}

watch(review, async () => {
  await nextTick()
  renderChart()
})

onMounted(load)
</script>

<style scoped>
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.muted { color: var(--color-muted); }
pre { white-space: pre-wrap; background: #f8fafc; padding: 12px; border-radius: 12px; }
.score { display: flex; gap: 12px; align-items: center; }
.big { font-size: 48px; font-family: var(--font-serif); }
.chart { width: 240px; height: 220px; }
.feedback { border-top: 1px solid var(--color-border); padding-top: 8px; }
@media (max-width: 900px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
