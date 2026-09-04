<template>
  <div class="page editor">
    <aside class="card prompt">
      <el-tag>{{ task?.taskType }}</el-tag>
      <h1>{{ task?.title }}</h1>
      <p>{{ task?.description }}</p>
      <p class="hint">建议字数：{{ minWords }} 词以上</p>
    </aside>
    <section class="card compose">
      <div class="meta">
        <span>当前 {{ wordCount }} 词</span>
        <div class="actions">
          <el-button @click="save(false)" :loading="saving">保存草稿</el-button>
          <el-button type="primary" @click="save(true)" :loading="saving">提交并批改</el-button>
        </div>
      </div>
      <el-input
        v-model="content"
        type="textarea"
        :autosize="{ minRows: 16, maxRows: 28 }"
        placeholder="在这里写英文作文…"
      />
      <div class="sample">
        <div class="meta">
          <strong>AI 范文</strong>
          <el-button size="small" :disabled="!essayId" :loading="sampling" @click="generateSample">生成范文</el-button>
        </div>
        <pre>{{ sample || '先保存草稿，再生成同题范文。' }}</pre>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { writingApi } from '../api'
import { streamEssaySample } from '../api/stream'
import type { WritingTask } from '../types'

const route = useRoute()
const router = useRouter()
const task = ref<WritingTask>()
const content = ref('')
const essayId = ref<number>()
const sample = ref('')
const saving = ref(false)
const sampling = ref(false)

const taskId = computed(() => Number(route.params.taskId))
const wordCount = computed(() => content.value.trim() ? content.value.trim().split(/\s+/).length : 0)
const minWords = computed(() => task.value?.taskType === 'TASK_1' ? 150 : 250)

onMounted(async () => {
  const res = await writingApi.task(taskId.value)
  task.value = res.data
})

async function save(submit: boolean) {
  saving.value = true
  try {
    const res = await writingApi.saveEssay({
      taskId: taskId.value,
      content: content.value,
      submit,
    })
    essayId.value = res.data.id
    ElMessage.success(submit ? '已提交' : '草稿已保存')
    if (submit) {
      await router.push(`/essays/${res.data.id}`)
    }
  } finally {
    saving.value = false
  }
}

async function generateSample() {
  if (!essayId.value) {
    await save(false)
  }
  if (!essayId.value) return
  sampling.value = true
  sample.value = ''
  try {
    await streamEssaySample(essayId.value, (chunk) => {
      sample.value += chunk
    })
  } catch {
    ElMessage.error('范文生成失败，请确认已配置 DeepSeek API Key')
  } finally {
    sampling.value = false
  }
}
</script>

<style scoped>
.editor {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: start;
}
.prompt h1 { font-size: 22px; }
.hint { color: var(--color-muted); }
.meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; gap: 8px; flex-wrap: wrap; }
.sample { margin-top: 16px; }
pre {
  white-space: pre-wrap;
  background: #f8fafc;
  padding: 12px;
  border-radius: 12px;
  min-height: 80px;
}
@media (max-width: 900px) {
  .editor { grid-template-columns: 1fr; }
}
</style>
