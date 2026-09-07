<template>
  <el-skeleton v-if="loading" animated :rows="rows" class="state-skeleton" />
  <el-result v-else-if="error" icon="error" title="加载失败" :sub-title="error" class="state-result">
    <template #extra>
      <el-button type="primary" @click="emit('retry')">重试</el-button>
    </template>
  </el-result>
  <el-empty v-else-if="empty" :description="emptyText" class="state-empty">
    <slot name="empty-action" />
  </el-empty>
  <slot v-else />
</template>

<script setup lang="ts">
/**
 * 列表/详情页通用状态壳：loading → 骨架屏；error → 失败 + 重试；empty → 空态；否则渲染 slot。
 */
withDefaults(
  defineProps<{
    loading: boolean
    error?: string | null
    empty?: boolean
    emptyText?: string
    rows?: number
  }>(),
  { error: null, empty: false, emptyText: '暂无数据', rows: 6 },
)
const emit = defineEmits<{ retry: [] }>()
</script>

<style scoped>
.state-skeleton { padding: 8px 0; }
.state-result, .state-empty { padding: 32px 0; }
</style>
