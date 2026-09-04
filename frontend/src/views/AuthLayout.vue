<template>
  <div class="auth-page">
    <aside class="brand-panel" aria-hidden="false">
      <div class="brand-mark">
        <span class="mark">A</span>
        <span>Alice Learn</span>
      </div>
      <p class="eyebrow">IELTS Writing Studio</p>
      <h1>{{ headline }}</h1>
      <p class="lead">{{ lead }}</p>
      <ul class="criteria">
        <li><span>TA</span> Task Response</li>
        <li><span>CC</span> Coherence</li>
        <li><span>LR</span> Lexical Resource</li>
        <li><span>GRA</span> Grammar</li>
      </ul>
      <div class="quote">
        <p>Write with structure. Revise with evidence. Aim for the band you named.</p>
      </div>
    </aside>
    <main class="form-panel">
      <div class="form-wrap">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  headline: string
  lead: string
}>()
</script>
