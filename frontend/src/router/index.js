import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'

/**
 * 根据用户角色获取默认首页
 */
function getDefaultRoute(role) {
  switch (role) {
    case 'pm': return '/dashboard'
    case 'dev_lead':
    case 'dev': return '/developer'
    case 'tester': return '/tester'
    default: return '/dashboard'
  }
}

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
    meta: { requiresAuth: true, roles: ['pm'] }
  },
  {
    path: '/developer',
    name: 'DeveloperDashboard',
    component: () => import('@/views/DeveloperDashboard.vue'),
    meta: { requiresAuth: true, roles: ['pm', 'dev_lead', 'dev'] }
  },
  {
    path: '/tester',
    name: 'TesterDashboard',
    component: () => import('@/views/TesterDashboard.vue'),
    meta: { requiresAuth: true, roles: ['pm', 'dev_lead', 'dev', 'tester'] }
  },
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('@/views/Projects.vue'),
    meta: { requiresAuth: true, roles: ['pm'] }
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import('@/views/Users.vue'),
    meta: { requiresAuth: true, roles: ['pm'] }
  },
  {
    path: '/requirements',
    name: 'Requirements',
    component: () => import('@/views/Requirements.vue'),
    meta: { requiresAuth: true, roles: ['pm', 'dev_lead'] }
  },
  {
    path: '/requirements/:id',
    name: 'RequirementDetail',
    component: () => import('@/views/RequirementDetail.vue'),
    meta: { requiresAuth: true, roles: ['pm', 'dev_lead'] }
  },
  {
    path: '/iterations',
    name: 'Iterations',
    component: () => import('@/views/Iterations.vue'),
    meta: { requiresAuth: true, roles: ['pm', 'dev_lead', 'dev', 'tester'] }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true, roles: ['pm', 'dev_lead', 'dev', 'tester'] }
  },
  {
    path: '/revenue',
    name: 'RevenueDashboard',
    component: () => import('@/views/RevenueDashboard.vue'),
    meta: { requiresAuth: true, roles: ['pm'] }
  },
  {
    path: '/performance',
    name: 'PerformanceStats',
    component: () => import('@/views/PerformanceStats.vue'),
    meta: { requiresAuth: true, roles: ['pm', 'dev_lead', 'dev', 'tester'] }
  },
  {
    path: '/systems',
    name: 'Systems',
    component: () => import('@/views/Systems.vue'),
    meta: { requiresAuth: true, roles: ['pm'] }
  },
  {
    path: '/dictionary',
    name: 'Dictionary',
    component: () => import('@/views/Dictionary.vue'),
    meta: { requiresAuth: true, roles: ['pm'] }
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

  // 角色权限校验
  if (to.meta.roles && authStore.userInfo) {
    const userRole = authStore.userInfo.role
    if (!to.meta.roles.includes(userRole)) {
      // 无权限访问，跳转到默认首页
      next(getDefaultRoute(userRole))
      return
    }
  }

  next()
})

export default router
