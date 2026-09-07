<template>
  <div class="page">
    <section class="card profile">
      <div>
        <p class="kicker">个人中心</p>
        <h1>{{ store.profile?.username }}</h1>
        <p class="muted">{{ store.profile?.email }} · 注册于 {{ formatTime(store.profile?.createdAt) }}</p>
      </div>
      <div class="target">
        <span class="muted">目标分数</span>
        <strong>{{ store.profile?.targetScore ?? '未设置' }}</strong>
      </div>
    </section>

    <el-tabs v-model="tab" class="tabs">
      <el-tab-pane label="作文记录" name="essays">
        <section class="card">
          <div class="toolbar">
            <h2>作文记录</h2>
            <el-radio-group v-model="essayStatus" size="small" @change="loadEssays(1)">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="DRAFT">草稿</el-radio-button>
              <el-radio-button value="SUBMITTED">已提交</el-radio-button>
              <el-radio-button value="REVIEWED">已批改</el-radio-button>
            </el-radio-group>
          </div>
          <AsyncState
            :loading="essays.loading"
            :error="essays.error"
            :empty="!essays.records.length"
            empty-text="还没有作文，去题库挑一道开始吧"
            :rows="5"
            @retry="loadEssays()"
          >
            <template #empty-action>
              <el-button type="primary" @click="$router.push('/writing')">去写作题库</el-button>
            </template>
            <el-table :data="essays.records" class="table">
              <el-table-column label="题目" min-width="220">
                <template #default="{ row }">
                  <el-tag size="small" class="type">{{ row.taskType ?? '-' }}</el-tag>
                  <span>{{ row.taskTitle }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="wordCount" label="词数" width="80" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="总分" width="80">
                <template #default="{ row }">
                  <strong v-if="row.overallScore != null">{{ row.overallScore }}</strong>
                  <span v-else class="muted">-</span>
                </template>
              </el-table-column>
              <el-table-column label="最近更新" width="150">
                <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status !== 'REVIEWED'"
                    text
                    type="primary"
                    @click="$router.push(`/writing/${row.taskId}?essayId=${row.id}`)"
                  >
                    继续写
                  </el-button>
                  <el-button text type="primary" @click="$router.push(`/essays/${row.id}`)">
                    {{ row.status === 'REVIEWED' ? '看报告' : '详情' }}
                  </el-button>
                  <el-button text type="danger" @click="removeEssay(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-if="essays.total > essays.size"
              class="pager"
              layout="prev, pager, next, total"
              :total="essays.total"
              :page-size="essays.size"
              v-model:current-page="essays.page"
              @current-change="loadEssays()"
            />
          </AsyncState>
        </section>
      </el-tab-pane>

      <el-tab-pane label="阅读记录" name="reading">
        <section class="card">
          <div class="toolbar">
            <h2>阅读练习记录</h2>
          </div>
          <AsyncState
            :loading="attempts.loading"
            :error="attempts.error"
            :empty="!attempts.records.length"
            empty-text="还没有阅读练习记录"
            :rows="5"
            @retry="loadAttempts()"
          >
            <template #empty-action>
              <el-button type="primary" @click="$router.push('/reading')">去做阅读</el-button>
            </template>
            <el-table :data="attempts.records" class="table">
              <el-table-column label="文章" min-width="220">
                <template #default="{ row }">
                  <el-tag v-if="row.difficulty" size="small" type="info" class="type">{{ row.difficulty }}</el-tag>
                  <span>{{ row.passageTitle }}</span>
                </template>
              </el-table-column>
              <el-table-column label="得分" width="120">
                <template #default="{ row }">
                  <strong :class="scoreClass(row)">{{ row.correctCount }} / {{ row.total }}</strong>
                </template>
              </el-table-column>
              <el-table-column label="用时" width="110">
                <template #default="{ row }">{{ formatDuration(row.timeSpentSec) }}</template>
              </el-table-column>
              <el-table-column label="时间" width="150">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="170" fixed="right">
                <template #default="{ row }">
                  <el-button text type="primary" @click="$router.push(`/reading/${row.passageId}?attemptId=${row.id}`)">
                    看解析
                  </el-button>
                  <el-button text @click="$router.push(`/reading/${row.passageId}`)">再练一次</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-if="attempts.total > attempts.size"
              class="pager"
              layout="prev, pager, next, total"
              :total="attempts.total"
              :page-size="attempts.size"
              v-model:current-page="attempts.page"
              @current-change="loadAttempts()"
            />
          </AsyncState>
        </section>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncState from '../components/AsyncState.vue'
import { meApi, writingApi } from '../api'
import { useUserStore } from '../stores/user'
import type { EssaySummary, ReadingAttempt } from '../types'
import { formatDuration, formatTime, statusText, statusType } from '../utils/format'

interface ListState<T> {
  records: T[]
  total: number
  page: number
  size: number
  loading: boolean
  error: string | null
}

function createList<T>(size: number): ListState<T> {
  return { records: [], total: 0, page: 1, size, loading: false, error: null }
}

const store = useUserStore()
const tab = ref<'essays' | 'reading'>('essays')
const essayStatus = ref('')
const essays = reactive(createList<EssaySummary>(10))
const attempts = reactive(createList<ReadingAttempt>(10))
const attemptsLoaded = ref(false)

async function loadEssays(page?: number) {
  if (page) essays.page = page
  essays.loading = true
  essays.error = null
  try {
    const res = await meApi.essays({ page: essays.page, size: essays.size, status: essayStatus.value || undefined })
    essays.records = res.data.records
    essays.total = res.data.total
  } catch (e) {
    essays.error = (e as Error).message
  } finally {
    essays.loading = false
  }
}

async function loadAttempts(page?: number) {
  if (page) attempts.page = page
  attempts.loading = true
  attempts.error = null
  try {
    const res = await meApi.reading({ page: attempts.page, size: attempts.size })
    attempts.records = res.data.records
    attempts.total = res.data.total
    attemptsLoaded.value = true
  } catch (e) {
    attempts.error = (e as Error).message
  } finally {
    attempts.loading = false
  }
}

async function removeEssay(row: EssaySummary) {
  try {
    await ElMessageBox.confirm(`删除「${row.taskTitle}」这篇作文？批改记录会一起删除。`, '删除作文', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await writingApi.deleteEssay(row.id)
  ElMessage.success('已删除')
  if (essays.records.length === 1 && essays.page > 1) essays.page -= 1
  await loadEssays()
}

function scoreClass(row: ReadingAttempt) {
  const ratio = row.total ? row.correctCount / row.total : 0
  return ratio >= 0.8 ? 'good' : ratio >= 0.5 ? 'ok' : 'bad'
}

watch(tab, (value) => {
  if (value === 'reading' && !attemptsLoaded.value) loadAttempts()
})

onMounted(() => loadEssays())
</script>

<style scoped>
.profile { display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap; }
.kicker { margin: 0 0 4px; color: var(--color-accent); font-size: 12px; font-weight: 700; letter-spacing: 0.14em; text-transform: uppercase; }
.profile h1 { margin: 0 0 6px; }
.target { text-align: right; }
.target strong { display: block; font-size: 36px; font-family: var(--font-serif); }
.tabs { margin-top: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 8px; }
.toolbar h2 { margin: 0; }
.type { margin-right: 8px; }
.table { width: 100%; }
.pager { margin-top: 16px; justify-content: flex-end; }
.good { color: #15803d; }
.ok { color: #b45309; }
.bad { color: #b91c1c; }
</style>
