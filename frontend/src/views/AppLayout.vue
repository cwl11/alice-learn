<template>
  <div class="shell">
    <header class="topbar">
      <router-link class="brand" to="/">
        <span class="mark">A</span>
        Alice Learn
      </router-link>
      <nav class="nav" aria-label="主导航">
        <router-link class="nav-link" to="/" active-class="nav-plain" exact-active-class="router-link-active">首页</router-link>
        <router-link class="nav-link" to="/writing">写作</router-link>
        <router-link class="nav-link" to="/vocab">词汇</router-link>
        <router-link class="nav-link" to="/reading">阅读</router-link>
        <router-link class="nav-link" to="/me">我的</router-link>
      </nav>
      <div class="user">
        <span>{{ store.profile?.username }}</span>
        <button type="button" class="logout" @click="onLogout">退出</button>
      </div>
    </header>
    <main class="shell-main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const store = useUserStore()
const router = useRouter()

function onLogout() {
  store.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
  background: var(--paper);
}

.topbar {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 14px clamp(20px, 6vw, 96px);
  background:
    linear-gradient(165deg, #211c3d 0%, #16132b 55%, #1e1b4b 100%);
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  color: #f6f1e8;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.mark {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: #c45c26;
  color: #fff7ed;
  font-family: var(--font-serif);
  font-size: 18px;
}

.nav {
  display: flex;
  gap: 6px;
  flex: 1;
}

.nav-link {
  padding: 8px 14px;
  border-radius: 999px;
  color: rgba(246, 241, 232, 0.72);
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  transition: background 180ms ease, color 180ms ease;
}

.nav-link.router-link-active,
.nav-link:hover {
  background: rgba(246, 241, 232, 0.1);
  color: #f6f1e8;
}

.user {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(246, 241, 232, 0.78);
}

.logout {
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid rgba(246, 241, 232, 0.2);
  border-radius: 999px;
  background: transparent;
  color: #f0b089;
  font-weight: 600;
}

.logout:hover {
  background: rgba(196, 92, 38, 0.2);
}

@media (max-width: 768px) {
  .topbar {
    flex-wrap: wrap;
    gap: 12px;
  }
  .nav {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }
}
</style>
