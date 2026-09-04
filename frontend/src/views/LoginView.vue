<template>
  <AuthLayout headline="把雅思写作练成日常。" lead="范文、四维批改、词汇卡片和阅读解析，收在同一张书桌前。">
    <p class="kicker">欢迎回来</p>
    <h2>登录账号</h2>
    <p class="sub">用你的用户名继续今天的练习。</p>
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="onSubmit">
      <div class="field-grid">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :prefix-icon="User" placeholder="例如 demo_user" autocomplete="username" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            type="password"
            show-password
            placeholder="输入密码"
            autocomplete="current-password"
            size="large"
          />
        </el-form-item>
      </div>
      <el-button type="primary" native-type="submit" :loading="loading" class="submit" size="large">进入学习台</el-button>
    </el-form>
    <p class="switch">还没有账号？<router-link to="/register">创建新账号</router-link></p>
  </AuthLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import AuthLayout from './AuthLayout.vue'
import { authApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await authApi.login(form)
    store.setToken(res.data.token)
    await store.fetchProfile()
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>
