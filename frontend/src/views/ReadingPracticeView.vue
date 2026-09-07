<template>
  <div class="page">
    <AsyncState :loading="loading" :error="error" :rows="10" @retry="load">
      <div class="reading" v-if="passage">
        <article class="card passage">
          <div class="head">
            <div>
              <el-tag size="small">{{ passage.difficulty }}</el-tag>
              <h1>{{ passage.title }}</h1>
            </div>
            <span class="timer" :title="reviewMode ? '本次用时' : '已用时间'">{{ timerText }}</span>
          </div>
          <p class="body">{{ passage.content }}</p>
        </article>
        <section class="card questions">
          <div class="head">
            <h2>题目</h2>
            <el-tag v-if="reviewMode" type="info" size="small">回顾模式</el-tag>
          </div>
          <el-alert
            v-if="reviewMode"
            type="info"
            :closable="false"
            show-icon
            title="正在查看历史作答；点击下方「再练一次」可重新作答。"
            class="notice"
          />
          <div v-for="(question, index) in passage.questions" :key="question.id" class="q">
            <p><strong>{{ index + 1 }}. </strong>{{ question.question }}</p>
            <el-radio-group
              v-if="question.options.length"
              v-model="answers[question.id]"
              :disabled="finished"
            >
              <el-radio v-for="option in question.options" :key="option" :value="option">{{ option }}</el-radio>
            </el-radio-group>
            <el-input v-else v-model="answers[question.id]" placeholder="填写答案" :disabled="finished" />
            <p v-if="resultMap[question.id]" class="result" :class="{ ok: resultMap[question.id].correct }">
              {{ resultMap[question.id].correct ? '正确' : '错误' }}
              · 答案：{{ resultMap[question.id].correctAnswer }}
              <template v-if="resultMap[question.id].explanation"> · {{ resultMap[question.id].explanation }}</template>
            </p>
          </div>
          <el-empty v-if="!passage.questions?.length" description="这篇文章还没有题目" />
          <div class="footer" v-else>
            <template v-if="!finished">
              <span class="muted">已答 {{ answeredCount }} / {{ passage.questions.length }}</span>
              <el-button type="primary" :loading="submitting" @click="submit">提交答案</el-button>
            </template>
            <template v-else>
              <strong class="score">得分 {{ summary?.correctCount }} / {{ summary?.total }}</strong>
              <div class="actions">
                <el-button @click="$router.push('/me')">查看记录</el-button>
                <el-button type="primary" @click="retry">再练一次</el-button>
              </div>
            </template>
          </div>
        </section>
      </div>
    </AsyncState>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncState from '../components/AsyncState.vue'
import { readingApi } from '../api'
import type { AnswerResultItem, ReadingPassage } from '../types'

const route = useRoute()
const router = useRouter()
const passage = ref<ReadingPassage>()
const answers = reactive<Record<number, string>>({})
const resultMap = reactive<Record<number, AnswerResultItem>>({})
const summary = ref<{ correctCount: number; total: number }>()
const loading = ref(true)
const error = ref<string | null>(null)
const submitting = ref(false)
const reviewMode = ref(false)

const elapsed = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const passageId = computed(() => Number(route.params.id))
const finished = computed(() => Boolean(summary.value))
const answeredCount = computed(
  () => passage.value?.questions?.filter((q) => (answers[q.id] ?? '').trim()).length ?? 0,
)
const timerText = computed(() => {
  const m = Math.floor(elapsed.value / 60)
  const s = elapsed.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

function startTimer() {
  stopTimer()
  elapsed.value = 0
  timer = setInterval(() => (elapsed.value += 1), 1000)
}
function stopTimer() {
  if (timer) clearInterval(timer)
  timer = null
}

function resetState() {
  Object.keys(answers).forEach((key) => delete answers[Number(key)])
  Object.keys(resultMap).forEach((key) => delete resultMap[Number(key)])
  summary.value = undefined
  reviewMode.value = false
}

function applyResult(items: AnswerResultItem[], correctCount: number, total: number) {
  items.forEach((item) => {
    resultMap[item.questionId] = item
    answers[item.questionId] = item.userAnswer
  })
  summary.value = { correctCount, total }
}

async function load() {
  loading.value = true
  error.value = null
  resetState()
  try {
    const res = await readingApi.passage(passageId.value)
    passage.value = res.data
    const attemptId = Number(route.query.attemptId)
    if (attemptId) {
      const attempt = await readingApi.attempt(attemptId)
      if (attempt.data.passageId !== passageId.value) {
        ElMessage.warning('该记录不属于这篇文章')
        await router.replace({ query: {} })
        startTimer()
      } else {
        reviewMode.value = true
        applyResult(attempt.data.items ?? [], attempt.data.correctCount, attempt.data.total)
        stopTimer()
        elapsed.value = attempt.data.timeSpentSec ?? 0
      }
    } else {
      startTimer()
    }
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!passage.value?.questions?.length) return
  const unanswered = passage.value.questions.length - answeredCount.value
  if (unanswered > 0) {
    try {
      await ElMessageBox.confirm(`还有 ${unanswered} 题未作答，确定提交吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '提交',
        cancelButtonText: '继续作答',
      })
    } catch {
      return
    }
  }
  submitting.value = true
  try {
    const res = await readingApi.submit({
      passageId: passageId.value,
      timeSpentSec: elapsed.value,
      answers: passage.value.questions.map((question) => ({
        questionId: question.id,
        answer: (answers[question.id] ?? '').trim(),
      })),
    })
    stopTimer()
    applyResult(res.data.items, res.data.correctCount, res.data.total)
    ElMessage.success(`提交成功，答对 ${res.data.correctCount} / ${res.data.total}`)
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function retry() {
  if (route.query.attemptId) {
    await router.replace({ query: {} })
  }
  resetState()
  startTimer()
}

watch(() => [route.params.id, route.query.attemptId], load)
onMounted(load)
onBeforeUnmount(stopTimer)
</script>

<style scoped>
.reading { display: grid; grid-template-columns: 1.2fr 1fr; gap: 16px; align-items: start; }
.head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 8px; }
.head h1 { margin: 8px 0 0; font-size: 24px; }
.head h2 { margin: 0; }
.timer { font-family: var(--font-serif); font-size: 22px; font-variant-numeric: tabular-nums; color: var(--color-accent); }
.body { white-space: pre-wrap; line-height: 1.7; }
.notice { margin-bottom: 12px; }
.q { margin-bottom: 16px; }
.result { color: #b91c1c; }
.result.ok { color: #15803d; }
.footer { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; border-top: 1px solid var(--color-border); padding-top: 12px; }
.score { font-size: 20px; font-family: var(--font-serif); }
.actions { display: flex; gap: 8px; }
@media (max-width: 900px) {
  .reading { grid-template-columns: 1fr; }
}
</style>
