<template>
  <div class="page">
    <section class="card">
      <h1>个人中心</h1>
      <p>用户名：{{ store.profile?.username }}</p>
      <p>邮箱：{{ store.profile?.email }}</p>
      <p>目标分数：{{ store.profile?.targetScore ?? '未设置' }}</p>
    </section>
    <section class="card" style="margin-top: 16px">
      <h2>作文记录</h2>
      <el-table :data="essays" empty-text="还没有作文">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskId" label="题目" width="80" />
        <el-table-column prop="wordCount" label="词数" width="90" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="createdAt" label="时间" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" @click="$router.push(`/essays/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { meApi } from '../api'
import { useUserStore } from '../stores/user'
import type { Essay } from '../types'

const store = useUserStore()
const essays = ref<Essay[]>([])

onMounted(async () => {
  const res = await meApi.essays({ page: 1, size: 20 })
  essays.value = res.data.records
})
</script>
