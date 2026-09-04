<template>
  <div class="page">
    <div class="toolbar">
      <h1>写作题库</h1>
      <el-radio-group v-model="taskType" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="TASK_1">Task 1</el-radio-button>
        <el-radio-button value="TASK_2">Task 2</el-radio-button>
      </el-radio-group>
    </div>
    <el-skeleton :loading="loading" animated :rows="6">
      <div class="list">
        <article v-for="task in tasks" :key="task.id" class="card item">
          <div>
            <el-tag size="small">{{ task.taskType }}</el-tag>
            <el-tag size="small" type="info" class="ml">{{ task.difficulty }}</el-tag>
            <h2>{{ task.title }}</h2>
            <p>{{ task.description }}</p>
          </div>
          <el-button type="primary" @click="router.push(`/writing/${task.id}`)">开始写作</el-button>
        </article>
      </div>
    </el-skeleton>
    <el-pagination
      v-if="total > size"
      class="pager"
      layout="prev, pager, next"
      :total="total"
      :page-size="size"
      v-model:current-page="page"
      @current-change="load"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { writingApi } from '../api'
import type { WritingTask } from '../types'

const router = useRouter()
const tasks = ref<WritingTask[]>([])
const total = ref(0)
const page = ref(1)
const size = 8
const taskType = ref('')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await writingApi.tasks({
      page: page.value,
      size,
      taskType: taskType.value || undefined,
    })
    tasks.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.list { display: grid; gap: 12px; }
.item { display: flex; justify-content: space-between; gap: 16px; align-items: start; }
.item h2 { margin: 8px 0 6px; font-size: 20px; }
.item p { margin: 0; color: var(--color-muted); }
.ml { margin-left: 6px; }
.pager { margin-top: 16px; }
@media (max-width: 768px) {
  .item { flex-direction: column; }
}
</style>
