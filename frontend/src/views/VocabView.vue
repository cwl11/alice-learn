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
    <div class="list">
      <article v-for="item in words" :key="item.id" class="card word">
        <div>
          <h2>{{ item.word }} <small>{{ item.phonetic }}</small></h2>
          <p>{{ item.meaning }}</p>
          <p class="muted">{{ item.example }}</p>
          <el-tag size="small">{{ item.category }}</el-tag>
        </div>
        <el-button v-if="!item.inNotebook" @click="add(item.id)">加入生词本</el-button>
        <el-button v-else type="danger" plain @click="remove(item.id)">移出</el-button>
      </article>
    </div>
    <el-pagination
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

async function load() {
  const api = tab.value === 'notebook'
    ? vocabApi.notebook({ page: page.value, size })
    : vocabApi.words({ page: page.value, size, keyword: keyword.value || undefined, category: category.value || undefined })
  const res = await api
  words.value = res.data.records
  total.value = res.data.total
}

async function add(id: number) {
  await vocabApi.add(id)
  await load()
}

async function remove(id: number) {
  await vocabApi.remove(id)
  await load()
}

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
.muted { color: var(--color-muted); }
.pager { margin-top: 16px; }
</style>
