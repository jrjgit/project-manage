import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const raw = localStorage.getItem('userInfo')
  const userInfo = ref((raw && raw !== 'undefined') ? JSON.parse(raw) : null)

  const role = computed(() => userInfo.value?.role || '')
  const isLoggedIn = computed(() => !!token.value)
  const isPM = computed(() => role.value === 'pm')
  const isDevLead = computed(() => role.value === 'dev_lead')
  const isDev = computed(() => role.value === 'dev')
  const isTesterLead = computed(() => role.value === 'tester_lead')
  const isTester = computed(() => role.value === 'tester')

  const setAuth = (t, u) => {
    token.value = t
    userInfo.value = u
    localStorage.setItem('token', t)
    localStorage.setItem('userInfo', JSON.stringify(u))
  }

  const clearAuth = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const doLogin = async (name, password) => {
    const res = await login({ name, password })
    setAuth(res.token, res.user)
    return res
  }

  const doRegister = async (data) => {
    return await register(data)
  }

  const logout = () => {
    clearAuth()
  }

  return {
    token,
    userInfo,
    role,
    isLoggedIn,
    isPM,
    isDevLead,
    isDev,
    isTesterLead,
    isTester,
    setAuth,
    clearAuth,
    doLogin,
    doRegister,
    logout
  }
})
