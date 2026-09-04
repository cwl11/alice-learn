<template>
  <div class="page reading" v-if="passage">
    <article class="card passage">
      <h1>{{ passage.title }}</h1>
      <p class="body">{{ passage.content }}</p>
    </article>
    <section class="card questions">
      <h2>题目</h2>
      <div v-for="(question, index) in passage.questions" :key="question.id" class="q">
        <p><strong>{{ index + 1 }}. </strong>{{ question.question }}</p>
        <el-radio-group v-if="question.options.length" v-model="answers[question.id]">
          <el-radio v-for="option in question.options" :key="option" :value="option">{{ option }}</el-radio>
        </el-radio-group>
        <el-input v-else v-model="answers[question.id]" placeholder="填写答案" />
        <p v-if="resultMap[question.id]" class="result" :class="{ ok: resultMap[question.id].correct }">
          {{ resultMap[question.id].correct ? '正确' : '错误' }}
          · 答案：{{ resultMap[question.id].correctAnswer }}
          · {{ resultMap[question.id].explanation }}
        </p>
      </div>
      <el-button type="primary" :loading="submitting" @click="submit">提交答案</el-button>
      <p v-if="summary">得分 {{ summary.correctCount }} / {{ summary.total }}</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { readingApi } from '../api'
import type { ReadingPassage, SubmitAnswersResponse } from '../types'

const route = useRoute()
const passage = ref<ReadingPassage>()
const answers = reactive<Record<number, string>>({})
const submitting = ref(false)
const summary = ref<SubmitAnswersResponse>()
const resultMap = reactive<Record<number, SubmitAnswersResponse['items'][number]>>({})

onMounted(async () => {
  const res = await readingApi.passage(Number(route.params.id))
  passage.value = res.data
})

async function submit() {
  if (!passage.value?.questions) return
  submitting.value = true
  try {
    const payload = passage.value.questions.map((question) => ({
      questionId: question.id,
      answer: answers[question.id] || '',
    }))
    const res = await readingApi.submit(payload)
    summary.value = res.data
    res.data.items.forEach((item) => {
      resultMap[item.questionId] = item
    })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.reading { display: grid; grid-template-columns: 1.2fr 1fr; gap: 16px; align-items: start; }
.body { white-space: pre-wrap; line-height: 1.7; }
.q { margin-bottom: 16px; }
.result { color: #b91c1c; }
.result.ok { color: #15803d; }
@media (max-width: 900px) {
  .reading { grid-template-columns: 1fr; }
}
</style>
