<template>
  <div class="page">
    <h1>记忆卡片</h1>
    <p class="muted">先把单词加入生词本，到期后会在这里出现。</p>
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
        <el-button @click="mark(false)">不记得</el-button>
        <el-button type="primary" @click="mark(true)">记得</el-button>
      </div>
      <p class="muted">剩余 {{ cards.length }} 张</p>
    </section>
    <el-empty v-else description="暂时没有到期卡片" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { vocabApi } from '../api'
import type { WordItem } from '../types'

const cards = ref<WordItem[]>([])
const show = ref(false)
const current = computed(() => cards.value[0])

async function load() {
  const res = await vocabApi.reviewCards()
  cards.value = res.data
  show.value = false
}

async function mark(remembered: boolean) {
  if (!current.value) return
  await vocabApi.review(current.value.id, remembered)
  cards.value = cards.value.slice(1)
  show.value = false
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
