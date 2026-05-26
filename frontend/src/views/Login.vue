<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="panel-left">
        <div class="deco-box"></div>
        <div class="deco-box"></div>
        <div class="deco-box"></div>
      </div>
      <div class="panel-right">
        <div class="brand">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="brand-icon">
            <rect x="3" y="3" width="18" height="18" rx="3"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
          <h1>项目管理系统</h1>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>账号</label>
            <input v-model="form.account" type="text" placeholder="请输入账号" autocomplete="username" />
          </div>

          <div class="form-group">
            <label>密码</label>
            <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = ref({
  account: '',
  password: ''
})

async function handleSubmit() {
  loading.value = true
  try {
    await authStore.doLogin(form.value.account, form.value.password)
    window.$message.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    console.error(e)
  }
  loading.value = false
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  padding: 20px;
}

.login-panel {
  display: flex;
  width: 780px;
  min-height: 440px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.06), 0 4px 16px rgba(0,0,0,.04);
}

.panel-left {
  width: 280px;
  background: linear-gradient(160deg, #1e293b, #334155);
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 40px 32px;
  justify-content: center;
  align-items: flex-start;
}

.deco-box {
  height: 6px;
  border-radius: 3px;
}

.deco-box:nth-child(1) {
  width: 120px;
  background: #6366f1;
}

.deco-box:nth-child(2) {
  width: 200px;
  background: #8b5cf6;
}

.deco-box:nth-child(3) {
  width: 80px;
  background: #a78bfa;
}

.panel-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 48px;
}

.brand {
  margin-bottom: 32px;
}

.brand-icon {
  width: 32px;
  height: 32px;
  color: #4f46e5;
  display: block;
  margin-bottom: 16px;
}

.brand h1 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  color: #1e293b;
  outline: none;
  transition: border-color .15s, box-shadow .15s;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,.12);
}

.form-group input::placeholder {
  color: #9ca3af;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: #4f46e5;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background .15s;
  margin-top: 6px;
}

.submit-btn:hover:not(:disabled) {
  background: #4338ca;
}

.submit-btn:disabled {
  opacity: .55;
  cursor: not-allowed;
}
</style>
