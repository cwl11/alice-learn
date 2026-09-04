<template>
  <div class="home">
    <section class="hero">
      <div class="hero-inner">
        <p class="kicker">IELTS Writing Studio</p>
        <h1>今天练哪一项，{{ store.profile?.username }}？</h1>
        <p class="lead">范文、四维批改、词汇卡片和阅读解析，收在同一张书桌前。</p>
        <ul class="criteria">
          <li><span>TA</span> Task Response</li>
          <li><span>CC</span> Coherence</li>
          <li><span>LR</span> Lexical Resource</li>
          <li><span>GRA</span> Grammar</li>
        </ul>
      </div>
    </section>
    <section class="modules" aria-label="练习模块">
      <router-link class="module" to="/writing">
        <span class="code">01</span>
        <h2>写作</h2>
        <p>Task 1 / Task 2 题库，AI 生成范文，按四项评分批改。</p>
        <span class="go">开始写作</span>
      </router-link>
      <router-link class="module" to="/vocab">
        <span class="code">02</span>
        <h2>词汇</h2>
        <p>约 300 个学术核心词，生词本和间隔重复记忆卡片。</p>
        <span class="go">去背单词</span>
      </router-link>
      <router-link class="module" to="/reading">
        <span class="code">03</span>
        <h2>阅读</h2>
        <p>短文练习，提交后立刻核对答案并查看解析。</p>
        <span class="go">开始阅读</span>
      </router-link>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../stores/user'
const store = useUserStore()
</script>

<style scoped>
.home {
  min-height: calc(100vh - 72px);
}

.hero {
  color: #f6f1e8;
  background:
    radial-gradient(circle at 12% 18%, rgba(196, 92, 38, 0.28), transparent 32%),
    radial-gradient(circle at 88% 82%, rgba(129, 140, 248, 0.22), transparent 28%),
    linear-gradient(165deg, #211c3d 0%, #16132b 55%, #1e1b4b 100%);
}

.hero-inner {
  padding: clamp(28px, 4vh, 56px) clamp(20px, 6vw, 96px) clamp(32px, 5vh, 56px);
}

.kicker {
  margin: 0 0 12px;
  color: #f0b089;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.hero h1 {
  margin: 0;
  font-size: clamp(32px, 3.2vw, 52px);
  line-height: 1.15;
}

.lead {
  max-width: 46ch;
  margin: 16px 0 0;
  color: rgba(246, 241, 232, 0.78);
  font-size: 18px;
}

.criteria {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin: 24px 0 0;
  padding: 0;
  list-style: none;
}

.criteria li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(246, 241, 232, 0.86);
}

.criteria span {
  min-width: 42px;
  height: 28px;
  padding: 0 8px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: rgba(246, 241, 232, 0.08);
  border: 1px solid rgba(246, 241, 232, 0.16);
  font-size: 11px;
  font-weight: 700;
}

.modules {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  padding: 28px clamp(20px, 6vw, 96px) 64px;
}

.module {
  display: flex;
  flex-direction: column;
  min-height: 220px;
  padding: 28px 24px;
  border-radius: 24px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  color: var(--ink);
  box-shadow: var(--shadow-card);
  transition: transform 180ms ease, border-color 180ms ease;
}

.module:hover {
  transform: translateY(-3px);
  border-color: #c45c26;
}

.code {
  color: #c45c26;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.module h2 {
  margin: 12px 0 8px;
  font-size: 32px;
}

.module p {
  margin: 0;
  color: var(--color-muted);
  flex: 1;
}

.go {
  margin-top: 24px;
  color: var(--color-primary);
  font-weight: 600;
}

@media (max-width: 900px) {
  .criteria,
  .modules {
    grid-template-columns: 1fr;
  }

  .hero h1 {
    max-width: none;
  }
}
</style>
