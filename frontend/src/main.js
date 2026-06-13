import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createDiscreteApi, zhCN, dateZhCN } from 'naive-ui'
import App from './App.vue'
import router from './router'
import './assets/tailwind.css'

const { message, dialog, notification } = createDiscreteApi(
  ['message', 'dialog', 'notification'],
  { configProviderProps: { locale: zhCN, dateLocale: dateZhCN } }
)

// 全局挂载，方便在api层使用
window.$message = message
window.$dialog = dialog
window.$notification = notification

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
