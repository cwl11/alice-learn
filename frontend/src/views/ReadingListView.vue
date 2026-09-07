<template>
  <div class="page">
    <div class="toolbar">
      <h1>阅读练习</h1>
      <span class="muted" v-if="total">共 {{ total }} 篇</span>
    </div>
    <AsyncState
      :loading="loading"
      :error="error"
      :empty="!passages.length"
      empty-text="还没有阅读文章"
      :rows="5"
      @retry="load"
    >
      <div class="list">
        <article v-for="item in passages" :key="item.id" class="card row">
          <div>
            <h2>{{ item.title }}</h2>
            <el-tag size="small">{{ item.difficulty }}</el-tag>
          </div>
          <el-button type="primary" @click="$router.push(`/reading/${item.id}`)">开始答题</el-button>
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
import { onMounted, ref } from 'vue'
import AsyncState from '../components/AsyncState.vue'
import { readingApi } from '../api'
import type { ReadingPassage } from '../types'

const passages = ref<ReadingPassage[]>([])
const total = ref(0)
const page = ref(1)
const size = 8
const loading = ref(false)
const error = ref<string | null>(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    const res = await readingApi.passages({ page: page.value, size })
    passages.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: baseline; gap: 12px; }
.list { display: grid; gap: 12px; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
h2 { margin: 0 0 8px; }
.pager { margin-top: 16px; }
@media (max-width: 640px) {
  .row { flex-direction: column; align-items: stretch; }
}
</style>
