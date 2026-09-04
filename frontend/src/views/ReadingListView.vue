<template>
  <div class="page">
    <h1>阅读练习</h1>
    <div class="list">
      <article v-for="item in passages" :key="item.id" class="card row">
        <div>
          <h2>{{ item.title }}</h2>
          <el-tag size="small">{{ item.difficulty }}</el-tag>
        </div>
        <el-button type="primary" @click="$router.push(`/reading/${item.id}`)">开始答题</el-button>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { readingApi } from '../api'
import type { ReadingPassage } from '../types'

const passages = ref<ReadingPassage[]>([])

onMounted(async () => {
  const res = await readingApi.passages({ page: 1, size: 10 })
  passages.value = res.data.records
})
</script>

<style scoped>
.list { display: grid; gap: 12px; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
h2 { margin: 0 0 8px; }
</style>
