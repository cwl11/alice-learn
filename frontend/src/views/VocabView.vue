<template>
  <div class="page">
    <div class="toolbar">
      <h1>词汇</h1>
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索单词" clearable @keyup.enter="load" />
        <el-select v-model="category" placeholder="分类" clearable @change="load" style="width: 160px">
          <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button @click="load">搜索</el-button>
        <el-button type="primary" @click="$router.push('/vocab/review')">记忆卡片</el-button>
      </div>
    </div>
    <el-radio-group v-model="tab" @change="load" class="tabs">
      <el-radio-button value="all">词库</el-radio-button>
      <el-radio-button value="notebook">生词本</el-radio-button>
    </el-radio-group>
    <AsyncState
      :loading="loading"
      :error="error"
      :empty="!words.length"
      :empty-text="emptyText"
      :rows="6"
      @retry="load"
    >
      <template #empty-action>
        <el-button v-if="tab === 'notebook'" type="primary" @click="tab = 'all'; load()">去词库挑词</el-button>
      </template>
      <div class="list">
        <article v-for="item in words" :key="item.id" class="card word">
          <div>
            <h2>{{ item.word }} <small>{{ item.phonetic }}</small></h2>
            <p>{{ item.meaning }}</p>
            <p class="muted">{{ item.example }}</p>
            <el-tag size="small">{{ item.category }}</el-tag>
            <el-tag v-if="item.inNotebook && item.familiarity != null" size="small" type="success" class="ml">
              熟悉度 {{ item.familiarity }}/5
            </el-tag>
          </div>
          <el-button v-if="!item.inNotebook" :loading="busyId === item.id" @click="add(item.id)">加入生词本</el-button>
          <el-button v-else type="danger" plain :loading="busyId === item.id" @click="remove(item.id)">移出</el-button>
        </article>
      </div>
    </AsyncState>
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
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AsyncState from '../components/AsyncState.vue'
import { vocabApi } from '../api'
import type { WordItem } from '../types'

const categories = ['academic', 'education', 'environment', 'technology', 'health', 'society', 'economy', 'culture']
const tab = ref('all')
const keyword = ref('')
const category = ref('')
const words = ref<WordItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 12
const loading = ref(false)
const error = ref<string | null>(null)
const busyId = ref<number>()

const emptyText = computed(() => {
  if (tab.value === 'notebook') return '生词本还是空的，去词库把不熟的词加进来'
  if (keyword.value || category.value) return '没有匹配的单词，换个关键词或分类试试'
  return '词库暂无数据'
})

async function load() {
  loading.value = true
  error.value = null
  try {
    const res = tab.value === 'notebook'
      ? await vocabApi.notebook({ page: page.value, size })
      : await vocabApi.words({ page: page.value, size, keyword: keyword.value || undefined, category: category.value || undefined })
    words.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function add(id: number) {
  busyId.value = id
  try {
    await vocabApi.add(id)
    const item = words.value.find((w) => w.id === id)
    if (item) {
      item.inNotebook = true
      item.familiarity = 0
    }
    ElMessage.success('已加入生词本')
  } finally {
    busyId.value = undefined
  }
}

async function remove(id: number) {
  busyId.value = id
  try {
    await vocabApi.remove(id)
    if (tab.value === 'notebook') {
      if (words.value.length === 1 && page.value > 1) page.value -= 1
      await load()
    } else {
      const item = words.value.find((w) => w.id === id)
      if (item) item.inNotebook = false
    }
    ElMessage.success('已移出生词本')
  } finally {
    busyId.value = undefined
  }
}

// 切换 tab / 筛选时回到第一页
watch([tab, category], () => {
  page.value = 1
})

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; align-items: center; }
.filters { display: flex; gap: 8px; flex-wrap: wrap; }
.tabs { margin: 12px 0; }
.list { display: grid; gap: 12px; }
.word { display: flex; justify-content: space-between; gap: 12px; align-items: start; }
.word h2 { margin: 0 0 6px; }
.word small { color: var(--color-muted); font-weight: 400; }
.ml { margin-left: 6px; }
.pager { margin-top: 16px; }
@media (max-width: 640px) {
  .word { flex-direction: column; }
}
</style>
