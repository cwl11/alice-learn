<template>
  <AuthLayout headline="从目标分数开始。" lead="注册后即可练写作、背核心词、做阅读。AI 批改按雅思四项给分。">
    <p class="kicker">创建账号</p>
    <h2>加入 Alice Learn</h2>
    <p class="sub">大约一分钟。之后就能选题写作。</p>
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="onSubmit">
      <div class="field-grid">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :prefix-icon="User" placeholder="至少 3 个字符" autocomplete="username" size="large" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" :prefix-icon="Message" placeholder="name@example.com" autocomplete="email" size="large" />
        </el-form-item>
      </div>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          :prefix-icon="Lock"
          type="password"
          show-password
          placeholder="至少 6 位"
          autocomplete="new-password"
          size="large"
        />
      </el-form-item>
      <el-form-item label="目标分数">
        <div class="score-pills" role="group" aria-label="目标分数">
          <button
            v-for="score in scores"
            :key="score"
            type="button"
            class="pill"
            :class="{ active: form.targetScore === score }"
            @click="form.targetScore = score"
          >
            {{ score.toFixed(1) }}
          </button>
        </div>
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="loading" class="submit" size="large">创建并开始</el-button>
    </el-form>
    <p class="switch">已有账号？<router-link to="/login">返回登录</router-link></p>
  </AuthLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AuthLayout from './AuthLayout.vue'
import { authApi } from '../api'

const scores = [6, 6.5, 7, 7.5, 8]
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: '',
  email: '',
  password: '',
  targetScore: 6.5,
})
const rules: FormRules = {
  username: [{ required: true, min: 3, message: '用户名至少 3 位', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入有效邮箱', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    await router.push({ name: 'login' })
  } finally {
    loading.value = false
  }
}
</script>
