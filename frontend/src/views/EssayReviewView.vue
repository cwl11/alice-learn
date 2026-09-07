<template>
  <div class="page">
    <AsyncState :loading="loading" :error="error" :rows="8" @retry="load">
      <div class="grid" v-if="essay">
        <section class="card">
          <div class="head">
            <div>
              <h1>{{ essay.taskTitle }}</h1>
              <p class="muted">
                {{ essay.taskType }} · {{ essay.wordCount }} 词 ·
                <el-tag size="small" :type="statusType(essay.status)">{{ statusText(essay.status) }}</el-tag>
              </p>
            </div>
            <div class="head-actions">
              <el-button
                v-if="essay.status !== 'REVIEWED'"
                text
                @click="$router.push(`/writing/${essay.taskId}?essayId=${essay.id}`)"
              >
                继续修改
              </el-button>
              <el-button text type="danger" @click="remove">删除</el-button>
            </div>
          </div>
          <p class="prompt">{{ essay.taskDescription }}</p>
          <pre>{{ essay.content }}</pre>
          <el-button
            type="primary"
            :loading="reviewing"
            :disabled="essay.status === 'DRAFT'"
            @click="runReview"
          >
            {{ review ? '重新批改' : 'AI 批改' }}
          </el-button>
          <p v-if="essay.status === 'DRAFT'" class="muted small">草稿需先提交后才能批改。</p>

          <details v-if="essay.sampleEssay" class="sample-box" open>
            <summary>同题 AI 范文</summary>
            <pre>{{ essay.sampleEssay }}</pre>
          </details>
        </section>

        <section class="card" v-if="review">
          <div class="score">
            <div>
              <p class="muted">总分</p>
              <strong class="big">{{ review.overallScore }}</strong>
              <p class="muted small">{{ formatTime(review.createdAt) }}</p>
            </div>
            <div ref="chartEl" class="chart" role="img" aria-label="四维分数雷达图"></div>
          </div>
          <ul class="bands">
            <li><span>TA</span>{{ review.taScore }}</li>
            <li><span>CC</span>{{ review.ccScore }}</li>
            <li><span>LR</span>{{ review.lrScore }}</li>
            <li><span>GRA</span>{{ review.graScore }}</li>
          </ul>
          <p>{{ review.comment }}</p>
          <h2>逐句建议</h2>
          <el-empty v-if="!review.feedback.length" description="本次没有逐句建议" :image-size="80" />
          <article v-for="(item, index) in review.feedback" :key="index" class="feedback">
            <p><strong>原文</strong> {{ item.original }}</p>
            <p><strong>建议</strong> {{ item.suggestion }}</p>
            <p class="muted">{{ item.reason }}</p>
          </article>
          <details v-if="review.sampleEssay" class="sample-box">
            <summary>改进示例段落</summary>
            <pre>{{ review.sampleEssay }}</pre>
          </details>
        </section>
        <section class="card placeholder" v-else>
          <el-empty description="还没有批改结果" :image-size="100">
            <p class="muted small">点击左侧「AI 批改」，约需 20–40 秒。</p>
          </el-empty>
        </section>
      </div>
    </AsyncState>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ECharts } from 'echarts'
import AsyncState from '../components/AsyncState.vue'
import { writingApi } from '../api'
import type { EssayDetail, EssayReview, EssayStatus } from '../types'
import { formatTime, statusText, statusType } from '../utils/format'

const route = useRoute()
const router = useRouter()
const essay = ref<EssayDetail>()
const review = ref<EssayReview>()
const loading = ref(true)
const error = ref<string | null>(null)
const reviewing = ref(false)
const chartEl = ref<HTMLDivElement>()
let chart: ECharts | null = null

const essayId = Number(route.params.essayId)

async function load() {
  loading.value = true
  error.value = null
  try {
    const res = await writingApi.essay(essayId)
    essay.value = res.data
    review.value = res.data.review
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function runReview() {
  reviewing.value = true
  try {
    const res = await writingApi.review(essayId)
    review.value = res.data
    if (essay.value) essay.value.status = 'REVIEWED' as EssayStatus
    ElMessage.success('批改完成')
  } catch {
    // 拦截器已提示
  } finally {
    reviewing.value = false
  }
}

async function remove() {
  try {
    await ElMessageBox.confirm('删除后不可恢复，批改记录也会一起删除。', '删除作文', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await writingApi.deleteEssay(essayId)
  ElMessage.success('已删除')
  router.replace('/me')
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
      color: '#c45c26',
    }],
  })
}

watch(review, async () => {
  await nextTick()
  renderChart()
})

onMounted(load)
onBeforeUnmount(() => {
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; align-items: start; }
.head { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; }
.head h1 { margin: 0 0 6px; font-size: 24px; }
.head-actions { display: flex; gap: 4px; flex-shrink: 0; }
.prompt { color: var(--color-muted); }
.small { font-size: 12px; }
pre { white-space: pre-wrap; background: #f8fafc; padding: 12px; border-radius: 12px; font-family: inherit; line-height: 1.7; }
.score { display: flex; gap: 12px; align-items: center; }
.big { font-size: 48px; font-family: var(--font-serif); }
.chart { width: 240px; height: 220px; }
.bands { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; padding: 0; margin: 8px 0 12px; list-style: none; }
.bands li { text-align: center; padding: 8px; border: 1px solid var(--color-border); border-radius: 12px; font-weight: 600; }
.bands span { display: block; font-size: 11px; color: var(--color-muted); letter-spacing: 0.08em; }
.feedback { border-top: 1px solid var(--color-border); padding-top: 8px; }
.sample-box { margin-top: 16px; }
.sample-box summary { cursor: pointer; font-weight: 600; margin-bottom: 8px; }
.placeholder { display: grid; place-items: center; min-height: 320px; }
@media (max-width: 900px) {
  .grid { grid-template-columns: 1fr; }
  .head { flex-direction: column; }
}
</style>
