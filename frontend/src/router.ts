import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from './stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('./views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('./views/RegisterView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('./views/AppLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('./views/HomeView.vue') },
        { path: 'writing', name: 'writing', component: () => import('./views/WritingListView.vue') },
        { path: 'writing/:taskId', name: 'writing-editor', component: () => import('./views/WritingEditorView.vue') },
        { path: 'essays/:essayId', name: 'essay-review', component: () => import('./views/EssayReviewView.vue') },
        { path: 'vocab', name: 'vocab', component: () => import('./views/VocabView.vue') },
        { path: 'vocab/review', name: 'vocab-review', component: () => import('./views/VocabReviewView.vue') },
        { path: 'reading', name: 'reading', component: () => import('./views/ReadingListView.vue') },
        { path: 'reading/:id', name: 'reading-practice', component: () => import('./views/ReadingPracticeView.vue') },
        { path: 'me', name: 'me', component: () => import('./views/ProfileView.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (to.meta.public) {
    return true
  }
  if (!store.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (!store.profile) {
    try {
      await store.fetchProfile()
    } catch {
      store.logout()
      return { name: 'login' }
    }
  }
  return true
})

export default router
