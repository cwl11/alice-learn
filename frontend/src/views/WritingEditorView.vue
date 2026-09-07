<template>
  <div class="page">
    <AsyncState :loading="loading" :error="error" :rows="8" @retry="load">
      <div class="editor" v-if="task">
        <aside class="card prompt">
          <div class="tags">
            <el-tag>{{ task.taskType }}</el-tag>
            <el-tag type="info">{{ task.difficulty }}</el-tag>
          </div>
          <h1>{{ task.title }}</h1>
          <p>{{ task.description }}</p>
          <p class="hint">建议字数：{{ minWords }} 词以上</p>
          <el-alert
            v-if="essayId"
            type="info"
            :closable="false"
            show-icon
            :title="resumeTitle"
            class="resume"
          />
        </aside>
        <section class="card compose">
          <div class="meta">
            <span :class="{ short: wordCount < minWords }">当前 {{ wordCount }} 词</span>
            <div class="actions">
              <el-button v-if="essayId" text @click="startNew">新开一篇</el-button>
              <el-button @click="save(false)" :loading="saving" :disabled="!content.trim()">保存草稿</el-button>
              <el-button type="primary" @click="save(true)" :loading="saving" :disabled="!content.trim()">
                提交
              </el-button>
            </div>
          </div>
          <el-input
            v-model="content"
            type="textarea"
            :autosize="{ minRows: 16, maxRows: 28 }"
            placeholder="在这里写英文作文…"
          />
          <p v-if="dirty" class="dirty">有未保存的修改</p>
          <div class="sample">
            <div class="meta">
              <strong>AI 范文</strong>
              <el-button size="small" :loading="sampling" @click="generateSample">
                {{ sample ? '重新生成' : '生成范文' }}
              </el-button>
            </div>
            <pre>{{ sample || '先保存草稿，再生成同题范文；生成后会随作文一起保存。' }}</pre>
          </div>
        </section>
      </div>
    </AsyncState>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncState from '../components/AsyncState.vue'
import { writingApi } from '../api'
import { streamEssaySample } from '../api/stream'
import type { Essay, WritingTask } from '../types'

const route = useRoute()
const router = useRouter()
const task = ref<WritingTask>()
const content = ref('')
const savedContent = ref('')
const essayId = ref<number>()
const essayStatus = ref<Essay['status']>()
const sample = ref('')
const loading = ref(true)
const error = ref<string | null>(null)
const saving = ref(false)
const sampling = ref(false)

const taskId = computed(() => Number(route.params.taskId))
const wordCount = computed(() => (content.value.trim() ? content.value.trim().split(/\s+/).length : 0))
const minWords = computed(() => (task.value?.taskType === 'TASK_1' ? 150 : 250))
const dirty = computed(() => content.value !== savedContent.value)
const resumeTitle = computed(() =>
  essayStatus.value === 'SUBMITTED' ? `正在修改已提交的作文 #${essayId.value}` : `正在继续草稿 #${essayId.value}`,
)

type EditableEssay = Pick<Essay, 'id' | 'status' | 'content' | 'sampleEssay'>

function applyEssay(essay: EditableEssay | null | undefined) {
  if (!essay) return
  essayId.value = essay.id
  essayStatus.value = essay.status
  content.value = essay.content
  savedContent.value = essay.content
  sample.value = essay.sampleEssay ?? ''
}

async function load() {
  loading.value = true
  error.value = null
  try {
    const res = await writingApi.task(taskId.value)
    task.value = res.data
    const queryEssayId = Number(route.query.essayId)
    if (queryEssayId) {
      const detail = await writingApi.essay(queryEssayId)
      if (detail.data.status === 'REVIEWED') {
        ElMessage.warning('该作文已批改，已为你新开一篇')
        await router.replace({ query: {} })
      } else {
        applyEssay(detail.data)
      }
    } else {
      const draft = await writingApi.latestDraft(taskId.value)
      applyEssay(draft.data)
    }
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function save(submit: boolean): Promise<boolean> {
  if (!content.value.trim()) return false
  saving.value = true
  try {
    const res = essayId.value
      ? await writingApi.updateEssay(essayId.value, { content: content.value, submit })
      : await writingApi.saveEssay({ taskId: taskId.value, content: content.value, submit })
    essayId.value = res.data.id
    essayStatus.value = res.data.status
    savedContent.value = content.value
    if (submit) {
      ElMessage.success('已提交，可在报告页发起 AI 批改')
      await router.push(`/essays/${res.data.id}`)
    } else {
      ElMessage.success('草稿已保存')
    }
    return true
  } catch {
    return false
  } finally {
    saving.value = false
  }
}

function startNew() {
  essayId.value = undefined
  essayStatus.value = undefined
  content.value = ''
  savedContent.value = ''
  sample.value = ''
  router.replace({ query: {} })
}

async function generateSample() {
  if (!essayId.value || dirty.value) {
    const ok = await save(false)
    if (!ok) return
  }
  if (!essayId.value) return
  sampling.value = true
  sample.value = ''
  try {
    await streamEssaySample(essayId.value, (chunk) => {
      sample.value += chunk
    })
  } catch (e) {
    ElMessage.error((e as Error).message || '范文生成失败，请确认已配置 DeepSeek API Key')
  } finally {
    sampling.value = false
  }
}

onBeforeRouteLeave(async () => {
  if (!dirty.value || saving.value) return true
  try {
    await ElMessageBox.confirm('有未保存的修改，确定离开吗？', '提示', {
      confirmButtonText: '离开',
      cancelButtonText: '留下',
      type: 'warning',
    })
    return true
  } catch {
    return false
  }
})

watch(() => route.params.taskId, load)
onMounted(load)
</script>

<style scoped>
.editor {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: start;
}
.tags { display: flex; gap: 6px; }
.prompt h1 { font-size: 22px; }
.hint { color: var(--color-muted); }
.resume { margin-top: 12px; }
.meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; gap: 8px; flex-wrap: wrap; }
.short { color: var(--color-accent); }
.dirty { margin: 6px 0 0; font-size: 12px; color: var(--color-muted); }
.sample { margin-top: 16px; }
pre {
  white-space: pre-wrap;
  background: #f8fafc;
  padding: 12px;
  border-radius: 12px;
  min-height: 80px;
  font-family: inherit;
}
@media (max-width: 900px) {
  .editor { grid-template-columns: 1fr; }
}
</style>
