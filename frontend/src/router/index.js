import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/developer',
    name: 'DeveloperDashboard',
    component: () => import('@/views/DeveloperDashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('@/views/Projects.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('@/views/Tasks.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks/:id',
    name: 'TaskDetail',
    component: () => import('@/views/Tasks.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/bugs',
    name: 'Bugs',
    component: () => import('@/views/Bugs.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/bugs/:id',
    name: 'BugDetail',
    component: () => import('@/views/Bugs.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import('@/views/Users.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/requirements',
    name: 'Requirements',
    component: () => import('@/views/Requirements.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/requirements/:id',
    name: 'RequirementDetail',
    component: () => import('@/views/RequirementDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/iterations',
    name: 'Iterations',
    component: () => import('@/views/Iterations.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/revenue',
    name: 'RevenueDashboard',
    component: () => import('@/views/RevenueDashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/performance',
    name: 'PerformanceStats',
    component: () => import('@/views/PerformanceStats.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/systems',
    name: 'Systems',
    component: () => import('@/views/Systems.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dictionary',
    name: 'Dictionary',
    component: () => import('@/views/Dictionary.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    next()
    return
  }

  if (to.meta.requiresAuth && !authStore.token) {
    next('/login')
    return
  }

  next()
})

export default router
