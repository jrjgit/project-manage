<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1>ProjectOS</h1>
        <p>极简企业项目管理系统</p>
      </div>
      <div class="feature-list">
        <div class="feature-item">
          <div class="feature-icon" style="background: linear-gradient(135deg, #6366f1, #8b5cf6)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <div>
            <div class="feature-title">任务流转自动化</div>
            <div class="feature-desc">状态变更自动触发通知，责任到人</div>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: linear-gradient(135deg, #10b981, #34d399)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          </div>
          <div>
            <div class="feature-title">五级角色权限</div>
            <div class="feature-desc">PM / 开发组长 / 开发 / 测试组长 / 测试</div>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
          </div>
          <div>
            <div class="feature-title">看板 + 列表双视图</div>
            <div class="feature-desc">拖拽变更状态，直观高效</div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <h2>{{ isLogin ? '欢迎回来' : '创建账号' }}</h2>
        <p class="subtitle">{{ isLogin ? '登录以继续管理您的项目' : '注册后开始使用系统' }}</p>

        <div class="form-toggle">
          <button :class="['toggle-btn', { active: isLogin }]" @click="isLogin = true">登录</button>
          <button :class="['toggle-btn', { active: !isLogin }]" @click="isLogin = false">注册</button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="form.name" type="text" placeholder="请输入用户名" required />
          </div>

          <div class="form-group">
            <label>密码</label>
            <input v-model="form.password" type="password" placeholder="至少6位字符" required minlength="6" />
          </div>

          <div v-if="!isLogin" class="form-group">
            <label>角色</label>
            <select v-model="form.role" required>
              <option value="" disabled>选择角色</option>
              <option value="pm">项目经理</option>
              <option value="dev_lead">开发组长</option>
              <option value="dev">开发</option>
              <option value="tester_lead">测试组长</option>
              <option value="tester">测试</option>
            </select>
          </div>

          <div v-if="!isLogin" class="form-group">
            <label>企业微信ID <span style="color:#94a3b8;font-size:12px">（用于接收通知）</span></label>
            <input v-model="form.wechat_id" type="text" placeholder="可选" />
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="!loading">{{ isLogin ? '登录' : '注册' }}</span>
            <span v-else>处理中...</span>
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
const isLogin = ref(true)
const loading = ref(false)

const form = ref({
  name: '',
  password: '',
  role: '',
  wechat_id: ''
})

async function handleSubmit() {
  loading.value = true
  try {
    if (isLogin.value) {
      await authStore.doLogin(form.value.name, form.value.password)
      window.$message.success('登录成功')
      router.push('/dashboard')
    } else {
      await authStore.doRegister(form.value)
      window.$message.success('注册成功，请登录')
      isLogin.value = true
      form.value = { name: '', password: '', role: '', wechat_id: '' }
    }
  } catch (e) {
    console.error(e)
  }
  loading.value = false
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: white;
}

.login-left {
  width: 45%;
  background: linear-gradient(160deg, #0f172a 0%, #1e293b 50%, #312e81 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  color: white;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(99,102,241,0.15) 0%, transparent 70%);
}

.brand {
  position: relative;
  z-index: 1;
  margin-bottom: 60px;
}

.brand-logo {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 10px 30px rgba(99,102,241,0.3);
}

.brand-logo svg {
  width: 28px;
  height: 28px;
  color: white;
}

.brand h1 {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
  margin-bottom: 8px;
}

.brand p {
  font-size: 16px;
  color: rgba(255,255,255,0.55);
}

.feature-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.feature-icon svg {
  width: 20px;
  height: 20px;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 13px;
  color: rgba(255,255,255,0.5);
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8fafc;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.login-box h2 {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 6px;
}

.subtitle {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 28px;
}

.form-toggle {
  display: flex;
  background: #f1f5f9;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 24px;
}

.toggle-btn {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn.active {
  background: white;
  color: #0f172a;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  margin-bottom: 6px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  background: white;
  color: #0f172a;
  transition: all 0.2s;
  outline: none;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}

.form-group input::placeholder {
  color: #94a3b8;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  box-shadow: 0 8px 20px rgba(99,102,241,0.35);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
