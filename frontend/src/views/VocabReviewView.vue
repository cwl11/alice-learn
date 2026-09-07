<template>
  <div class="page">
    <h1>记忆卡片</h1>
    <p class="muted">先把单词加入生词本，到期后会在这里出现。</p>
    <AsyncState
      :loading="loading"
      :error="error"
      :empty="!current"
      :empty-text="finishedCount ? `本轮完成，复习了 ${finishedCount} 张卡片` : '暂时没有到期卡片'"
      :rows="5"
      @retry="load"
    >
      <template #empty-action>
        <el-button type="primary" @click="$router.push('/vocab')">回到词库</el-button>
      </template>
      <section v-if="current" class="card flip-wrap">
        <button class="flip-card" type="button" :class="{ show }" @click="show = !show" :aria-pressed="show">
          <div class="face front">
            <h2>{{ current.word }}</h2>
            <p>{{ current.phonetic }}</p>
            <span>点击查看释义</span>
          </div>
          <div class="face back">
            <h2>{{ current.meaning }}</h2>
            <p>{{ current.example }}</p>
          </div>
        </button>
        <div class="actions">
          <el-button :loading="marking" @click="mark(false)">不记得</el-button>
          <el-button type="primary" :loading="marking" @click="mark(true)">记得</el-button>
        </div>
        <p class="muted">剩余 {{ cards.length }} 张</p>
      </section>
    </AsyncState>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AsyncState from '../components/AsyncState.vue'
import { vocabApi } from '../api'
import type { WordItem } from '../types'

const cards = ref<WordItem[]>([])
const show = ref(false)
const loading = ref(false)
const error = ref<string | null>(null)
const marking = ref(false)
const finishedCount = ref(0)
const current = computed(() => cards.value[0])

async function load() {
  loading.value = true
  error.value = null
  finishedCount.value = 0
  try {
    const res = await vocabApi.reviewCards()
    cards.value = res.data
    show.value = false
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function mark(remembered: boolean) {
  if (!current.value || marking.value) return
  marking.value = true
  try {
    await vocabApi.review(current.value.id, remembered)
    cards.value = cards.value.slice(1)
    finishedCount.value += 1
    show.value = false
  } finally {
    marking.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.muted { color: var(--color-muted); }
.flip-wrap { max-width: 520px; }
.flip-card {
  width: 100%;
  min-height: 220px;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  background: #eef2ff;
  position: relative;
  cursor: pointer;
}
.face { padding: 32px 20px; }
.back { display: none; }
.show .front { display: none; }
.show .back { display: block; }
.actions { display: flex; gap: 12px; margin-top: 16px; }
.actions .el-button { min-height: 44px; flex: 1; }
</style>
