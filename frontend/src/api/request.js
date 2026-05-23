import axios from 'axios'
import { useAuthStore } from '@/store/useAuthStore'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const body = response.data
    // Result<T> wrapper: { data: ... } → unwrap
    if (body && body.data !== undefined) return body.data
    return body
  },
  (error) => {
    const msg = error.response?.data?.error || error.message || '请求失败'
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    }
    if (window.$message) {
      window.$message.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
