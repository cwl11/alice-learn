import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '../api'
import type { UserProfile } from '../types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const profile = ref<UserProfile | null>(null)

  const isLoggedIn = computed(() => Boolean(token.value))

  function setToken(value: string) {
    token.value = value
    localStorage.setItem('token', value)
  }

  function logout() {
    token.value = ''
    profile.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  async function fetchProfile() {
    if (!token.value) return
    const res = await authApi.me()
    profile.value = res.data
  }

  return { token, profile, isLoggedIn, setToken, logout, fetchProfile }
})
